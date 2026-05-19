package com.collabdoc.websocket;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.View;
import org.springframework.web.socket.WebSocketSession;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

@Component
public class UserSessionManager {

    //ConcurrentHashMap：线程安全的Map，key是userId,value是用户的所有会话
    //CopyOnWriteArraySet：线程安全的Set,适合读多写少（遍历发送消息的场景）
    //userId -> 用户的多个会话（同一用户多个标签）
    private final ConcurrentHashMap<String, CopyOnWriteArraySet<WebSocketSession>> userSessions = new ConcurrentHashMap<>();

    //computeIfAbsent：如果userId不存在，创建一个新的Set,如果存在，返回已有的
    //效果：用户的第一标签页创建Set，后续标签页直接加入
    //添加会话
    public void addSession(String userId,WebSocketSession session){
        userSessions.computeIfAbsent(userId,k -> new CopyOnWriteArraySet<>()).add(session);
    }

    //移除会话
    public void removeSession(String userId,WebSocketSession session){
        CopyOnWriteArraySet<WebSocketSession> sessions = userSessions.get(userId);  //获取用户的所有会话
        if (sessions!=null){
            sessions.remove(session);   //移除指定的会话
            if (sessions.isEmpty()){
                userSessions.remove(userId);    //如果该用户没有其他会话了，从Map中删除这个用户
            }
        }
    }

    //获取用户的所有会话
    //用于向用户的所有标签页发消息
    public CopyOnWriteArraySet<WebSocketSession> getserSessions(String userId){
        return userSessions.get(userId);
    }

    //向用户的所有会话发送消息
    //遍历用户的所有会话，检查会话是否还开着，向每个会话发送消息（所有标签页都能收到）
    public void sendToUser(String userId,String message){
        CopyOnWriteArraySet<WebSocketSession> sessions = userSessions.get(userId);
        if (sessions!=null){
            for (WebSocketSession session :sessions){
                try {
                    if (session.isOpen()){
                        session.sendMessage(new org.springframework.web.socket.TextMessage(message));
                    }
                }catch (Exception e){
                    System.err.println("发送消息失败：" + e.getMessage());
                }
            }
        }
    }
}
