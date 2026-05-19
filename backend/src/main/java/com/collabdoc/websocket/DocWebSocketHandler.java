package com.collabdoc.websocket;

import com.collabdoc.service.DocumentService;
import com.collabdoc.service.PermissionService;
import com.collabdoc.dto.WebSocketMessage;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;

/**
 * WebSocket 消息处理器
 *
 * 职责：
 * 1. 处理 WebSocket 连接建立、关闭、传输错误
 * 2. 处理 join（加入房间）和 update（编辑内容）消息
 * 3. 心跳检测
 * 4. 权限校验
 *
 * 线程安全说明：
 *   - 使用 ConcurrentHashMap 为每个 session 维护独立的锁对象
 *   - 所有 sendMessage 操作通过 safeSendMessage() 加锁，避免并发写入冲突
 *
 * @author xiaozhen
 */
@Component
public class DocWebSocketHandler extends TextWebSocketHandler {

    private final RoomManager roomManager;
    private final DocumentService documentService;
    private final PermissionService permissionService;
    private final ObjectMapper mapper = new ObjectMapper();
    private final Executor webSocketExecutor;

    // 全局 session 锁映射，保证同一个 session 的 sendMessage 串行执行
    private static final ConcurrentHashMap<String, Object> sessionLocks = new ConcurrentHashMap<>();

    public DocWebSocketHandler(RoomManager roomManager, DocumentService documentService,
                               PermissionService permissionService, Executor webSocketExecutor) {
        this.roomManager = roomManager;
        this.documentService = documentService;
        this.permissionService = permissionService;
        this.webSocketExecutor = webSocketExecutor;
    }

    /**
     * 获取指定 session 的锁对象
     * @param session WebSocket 会话
     * @return 该 session 对应的锁对象
     */
    private Object getLock(WebSocketSession session) {
        return sessionLocks.computeIfAbsent(session.getId(), k -> new Object());
    }

    /**
     * 线程安全地发送消息
     * 使用 session 级别的锁，避免并发写入导致的 TEXT_PARTIAL_WRITING 错误
     *
     * @param session 目标会话
     * @param message 要发送的文本消息
     * @throws IOException 发送失败时抛出
     */
    private void safeSendMessage(WebSocketSession session, String message) throws IOException {
        synchronized (getLock(session)) {
            if (session.isOpen()) {
                session.sendMessage(new TextMessage(message));
            }
        }
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        System.out.println("WebSocket 连接建立：" + session.getId());
        System.out.println("连接 URI：" + session.getUri());

        String query = session.getUri().getQuery();
        String userId = null;
        if (query != null && query.contains("userId=")) {
            userId = query.split("userId=")[1].split("&")[0];
        }

        if (userId == null) userId = "anonymous";
        session.getAttributes().put("userId", userId);
        System.out.println("userId:" + userId);
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        webSocketExecutor.execute(() -> {
            try {
                processMessage(session, message);
            } catch (Exception e) {
                System.err.println("处理消息失败：" + e.getMessage());
            }
        });
    }

    private void processMessage(WebSocketSession session, TextMessage message) throws Exception {
        String payload = message.getPayload();

        if (payload == null || payload.trim().isEmpty()) {
            System.out.println("收到空消息，忽略");
            return;
        }

        if ("ping".equals(payload)) {
            safeSendMessage(session, "pong");
            return;
        }

        WebSocketMessage msg = mapper.readValue(payload, WebSocketMessage.class);
        String type = msg.getType();

        if ("join".equals(type)) {
            String docId = msg.getDocId();
            roomManager.joinRoom(docId, session);

            String content = documentService.getContent(docId);

            WebSocketMessage reply = new WebSocketMessage();
            reply.setType("init");
            reply.setContent(content == null ? "" : content);
            String replyJson = mapper.writeValueAsString(reply);

            safeSendMessage(session, replyJson);

        } else if ("update".equals(type)) {
            String docId = msg.getDocId();
            String newContent = msg.getContent();
            String userId = (String) session.getAttributes().get("userId");

            if (!permissionService.hasPermission(docId, userId, "write")) {
                System.out.println("用户无编辑权限：" + userId);
                return;
            }

            documentService.saveContent(docId, newContent);
            roomManager.broadcastToRoom(docId, session, payload);
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        // 清理锁对象，防止内存泄漏
        sessionLocks.remove(session.getId());
        roomManager.leaveRoom(session);
        System.out.println("WebSocket 连接关闭：" + session.getId());
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        String msg = exception != null ? exception.getMessage() : "unknown";
        System.err.println("WebSocket 传输错误：" + (msg != null ? msg : "无详细信息"));
        sessionLocks.remove(session.getId());
        roomManager.leaveRoom(session);
    }
}