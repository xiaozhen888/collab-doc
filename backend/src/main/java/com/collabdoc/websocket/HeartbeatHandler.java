package com.collabdoc.websocket;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Component
public class HeartbeatHandler extends TextWebSocketHandler {

    //记录最后心跳时间（key:sessionId,value:最后心跳的时间戳）
    private final ConcurrentHashMap<String,Long> lastHeartbeat = new ConcurrentHashMap<>();
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
            session.sendMessage(new TextMessage("pong"));
        }else {
            //其他消息交给主处理器
            super.handleTextMessage(session,message);
        }
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception{
        lastHeartbeat.put(session.getId(),System.currentTimeMillis());  //连接建立时，记录当前时间作为第一次心跳时间
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception{
        lastHeartbeat.remove(session.getId());      //连接关闭时，从Map中移除记录
    }

    private void checkHeartbeat(){
        //心跳检测只需要知道“距离上次心跳过了多久”，不需要知道具体的时间
        long now = System.currentTimeMillis();  //系统毫秒数
        lastHeartbeat.forEach((sessionId,lastTime) -> {
            //只比较时间差，毫秒数直接相减更为简单
            if (now - lastTime > 60000){    //60秒无心跳，判定为死连接，打印日志并移除
                System.out.println("会话 " + sessionId + " 心跳超时，已断开");
                lastHeartbeat.remove(sessionId);
                //这里可以触发断开连接
            }
        });
    }
}
