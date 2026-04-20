package com.collabdoc.websocket;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * 房间管理器
 *
 * 职责：
 * 1. 管理文档房间（docId -> 会话集合）
 * 2. 用户加入/离开房间
 * 3. 广播消息给房间内其他用户
 * 4. 清理无效会话
 */
@Component
public class RoomManager {

    //ConcurrentHashMap：线程安全的Map,支持多线程并发访问
    //CopyOnWriteArraySet：线程安全的Set，适合读多写少的场景
    //docId -> 该文档下的所有对话
    public final ConcurrentHashMap<String, CopyOnWriteArraySet<WebSocketSession>> rooms = new ConcurrentHashMap<>();

    //sessionId -> docId(快速反向查找)
    private final ConcurrentHashMap<String,String> sessionToDoc = new ConcurrentHashMap<>();

    /**
     * 将用户加入房间
     *
     * @param docId 文档 ID（房间号）
     * @param session 用户会话
     */
    public void joinRoom(String docId,WebSocketSession session){
        cleanInvalidSession(docId);
        rooms.computeIfAbsent(docId,k -> new CopyOnWriteArraySet<>()).add(session);
        sessionToDoc.put(session.getId(),docId);
        System.out.println("用户" + session.getId() + "加入文档" + docId + ",当前人数：" + getRoomSize(docId));

        //广播在线人数
        broadcastPresencce(docId);
    }

    /**
     * 将用户移出房间
     */
    public void leaveRoom(WebSocketSession session){
        String docId = sessionToDoc.remove(session.getId());
        if (docId!=null){
            CopyOnWriteArraySet<WebSocketSession> room = rooms.get(docId);
            if (room!=null){
                room.remove(session);
                System.out.println("用户" + session.getId() + "离开文档" + docId + "，剩余人数" + room.size());
//                if (room.isEmpty()) rooms.remove(docId);
                cleanInvalidSession(docId);
                //广播在线人数
                broadcastPresencce(docId);
            }
        }
    }

    /**
     * 广播消息
     */
    public void broadcastToRoom(String docId,WebSocketSession sender,String message){
        cleanInvalidSession(docId);
        CopyOnWriteArraySet<WebSocketSession> room = rooms.get(docId);  //从房间Map中取出这个文档的所有会话
        if (room==null) return; //房间没人，直接返回

        for (WebSocketSession session:room){    //遍历房间中的每个用户
            if (session!=sender && session.isOpen()){   //排除发送者自己 且 连接还开着
                try {
                    session.sendMessage(new org.springframework.web.socket.TextMessage(message));
                } catch (IOException e) {
                    System.err.println("广播失败：" + e.getMessage());
                }
            }
        }
    }

    /**
     * 广播在线人数
     */
    private void broadcastPresencce(String docId){
        cleanInvalidSession(docId);
        CopyOnWriteArraySet<WebSocketSession> room = rooms.get(docId);
        if (room == null || room.isEmpty()) return;

        //统计连接数
        //room.size()本意就是连接数，只不过之前把他直接作为用户数在网页呈现
        int connectionCount = room.size();

        //统计用户数（按userId去重）
        Set<String> uniqueUsers = new HashSet<>();
        for (WebSocketSession session : room){  //遍历房间内的每个WebSocket连接
            //session.getAttributes()是一个Map,可以存放和这个连接相关的附加数据
            String userId = (String) session.getAttributes().get("userId");
            //如果userId存在，就加入HashSet,因为HashSet会自动去重，同一个userId只会被计入一次
            if (userId!=null) uniqueUsers.add(userId);
        }
        int userCount = uniqueUsers.size();

        //发送两个数据
        String message = String.format(
                "{\"type\":\"presence\",\"connectionCount\":%d,\"userCount\":%d}",
                connectionCount,userCount
        );

        for (WebSocketSession session : room){
            try {
                if (session.isOpen()) session.sendMessage(new TextMessage(message));
            } catch (IOException e) {
                System.err.println("发送在线人数失败：" + e.getMessage());
            }
        }

    }

    /**
     * 清理房间内的无效会话
     * 可改用心跳检测实现
     */
    private void cleanInvalidSession(String docId){
        CopyOnWriteArraySet<WebSocketSession> room = rooms.get(docId);
        if (room != null){
            room.removeIf(session -> !session.isOpen());
            if (room.isEmpty()) rooms.remove(docId);
        }
    }

    /**
     * 获取房间内人数
     */
    public int getRoomSize(String docId){
        cleanInvalidSession(docId);
        CopyOnWriteArraySet<WebSocketSession> room = rooms.get(docId);
        return room == null ? 0 :room.size();
    }

    /**
     * 获取用户所在文档的Id
     */
    public String getDocIdBySession(WebSocketSession session){
        return sessionToDoc.get(session.getId());
    }
}
