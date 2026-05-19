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
 *
 * 线程安全说明：
 *   - 使用 ConcurrentHashMap 和 CopyOnWriteArraySet 保证并发安全
 *   - 发送消息时使用 synchronized(session) 避免 TEXT_PARTIAL_WRITING 错误
 */
@Component
public class RoomManager {

    // docId -> 该文档下的所有会话
    public final ConcurrentHashMap<String, CopyOnWriteArraySet<WebSocketSession>> rooms = new ConcurrentHashMap<>();

    // sessionId -> docId(快速反向查找)
    private final ConcurrentHashMap<String, String> sessionToDoc = new ConcurrentHashMap<>();

    /**
     * 将用户加入房间
     *
     * @param docId 文档 ID（房间号）
     * @param session 用户会话
     */
    public void joinRoom(String docId, WebSocketSession session) {
        cleanInvalidSession(docId);
        rooms.computeIfAbsent(docId, k -> new CopyOnWriteArraySet<>()).add(session);
        sessionToDoc.put(session.getId(), docId);
        System.out.println("用户" + session.getId() + "加入文档" + docId + ",当前人数：" + getRoomSize(docId));

        // 广播在线人数
        broadcastPresencce(docId);
    }

    /**
     * 将用户移出房间
     */
    public void leaveRoom(WebSocketSession session) {
        String docId = sessionToDoc.remove(session.getId());
        if (docId != null) {
            CopyOnWriteArraySet<WebSocketSession> room = rooms.get(docId);
            if (room != null) {
                room.remove(session);
                System.out.println("用户" + session.getId() + "离开文档" + docId + "，剩余人数" + room.size());
                cleanInvalidSession(docId);
                // 广播在线人数
                broadcastPresencce(docId);
            }
        }
    }

    /**
     * 广播消息给房间内其他用户（排除发送者）
     *
     * @param docId 文档ID
     * @param sender 发送者会话
     * @param message 消息内容
     */
    public void broadcastToRoom(String docId, WebSocketSession sender, String message) {
        cleanInvalidSession(docId);
        CopyOnWriteArraySet<WebSocketSession> room = rooms.get(docId);
        if (room == null) return;

        for (WebSocketSession session : room) {
            // 排除发送者自己
            if (session.equals(sender)) continue;

            try {
                // 使用 synchronized 保证线程安全，避免并发写入冲突
                synchronized (session) {
                    if (session.isOpen()) {
                        session.sendMessage(new TextMessage(message));
                    }
                }
            } catch (IOException e) {
                System.err.println("广播消息失败: " + e.getMessage());
            }
        }
    }

    /**
     * 广播在线人数
     */
    private void broadcastPresencce(String docId) {
        cleanInvalidSession(docId);
        CopyOnWriteArraySet<WebSocketSession> room = rooms.get(docId);
        if (room == null || room.isEmpty()) return;

        int connectionCount = room.size();

        Set<String> uniqueUsers = new HashSet<>();
        for (WebSocketSession session : room) {
            String userId = (String) session.getAttributes().get("userId");
            if (userId != null) uniqueUsers.add(userId);
        }
        int userCount = uniqueUsers.size();

        String message = String.format(
                "{\"type\":\"presence\",\"connectionCount\":%d,\"userCount\":%d}",
                connectionCount, userCount
        );

        for (WebSocketSession session : room) {
            try {
                synchronized (session) {
                    if (session.isOpen()) {
                        session.sendMessage(new TextMessage(message));
                    }
                }
            } catch (IOException e) {
                System.err.println("发送在线人数失败：" + e.getMessage());
            }
        }
    }

    /**
     * 清理房间内的无效会话
     */
    private void cleanInvalidSession(String docId) {
        CopyOnWriteArraySet<WebSocketSession> room = rooms.get(docId);
        if (room != null) {
            room.removeIf(session -> !session.isOpen());
            if (room.isEmpty()) rooms.remove(docId);
        }
    }

    /**
     * 获取房间内人数
     */
    public int getRoomSize(String docId) {
        cleanInvalidSession(docId);
        CopyOnWriteArraySet<WebSocketSession> room = rooms.get(docId);
        return room == null ? 0 : room.size();
    }

    /**
     * 获取用户所在文档的Id
     */
    public String getDocIdBySession(WebSocketSession session) {
        return sessionToDoc.get(session.getId());
    }
}