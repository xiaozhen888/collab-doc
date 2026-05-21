package com.collabdoc.websocket;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Component
public class HeartbeatHandler extends TextWebSocketHandler {

    //记录最后心跳时间（key:sessionId,value:最后心跳的时间戳）
    private final ConcurrentHashMap<String,Long> lastHeartbeat = new ConcurrentHashMap<>();
    // 持有 session 引用，用于超时后主动关闭
    private final ConcurrentHashMap<String, WebSocketSession> sessions = new ConcurrentHashMap<>();
    //创建单线程的定时任务执行器
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    public HeartbeatHandler(){
        //每30秒检查一次心跳
        //scheduleAtFixedRate：定时执行任务
        //this::checkHeartbeat：要执行的方法
        //30,30, TimeUnit.SECONDS：延迟30秒执行，之后每30秒执行一次
        scheduler.scheduleAtFixedRate(this::checkHeartbeat,30,30, TimeUnit.SECONDS);
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception{
        String payload = message.getPayload();
        if ("ping".equals(payload)){
            //收到心跳，更新最后时间
            lastHeartbeat.put(session.getId(),System.currentTimeMillis());
            sessions.putIfAbsent(session.getId(), session);
            session.sendMessage(new TextMessage("pong"));
        }else {
            //其他消息交给主处理器
            super.handleTextMessage(session,message);
        }
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception{
        //连接建立时，记录当前时间作为第一次心跳时间
        long now = System.currentTimeMillis();
        lastHeartbeat.put(session.getId(), now);
        sessions.put(session.getId(), session);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception{
        // 连接关闭时清理所有状态
        cleanupSession(session.getId());
    }

    /**
     * 清理会话的所有状态
     */
    private void cleanupSession(String sessionId) {
        lastHeartbeat.remove(sessionId);
        sessions.remove(sessionId);
    }

    private void checkHeartbeat(){
        //心跳检测只需要知道“距离上次心跳过了多久”，不需要知道具体的时间
        long now = System.currentTimeMillis();  //系统毫秒数
        lastHeartbeat.forEach((sessionId,lastTime) -> {
            //只比较时间差，毫秒数直接相减更为简单
            if (now - lastTime > 60000){    //60秒无心跳，判定为死连接
                System.out.println("会话 " + sessionId + " 心跳超时，准备断开");
                WebSocketSession session = sessions.get(sessionId);
                if (session != null && session.isOpen()) {
                    try {
                        // 主动关闭连接，触发 afterConnectionClosed
                        session.close(CloseStatus.GOING_AWAY);
                        System.out.println("会话 " + sessionId + " 已主动关闭");
                    } catch (IOException e) {
                        System.err.println("关闭超时会话失败: " + e.getMessage());
                        // 即使 close 失败，也要强制清理本地状态
                        cleanupSession(sessionId);
                    }
                } else {
                    // session 已经关闭或不存在，只清理本地状态
                    cleanupSession(sessionId);
                }
            }
        });
    }
}
