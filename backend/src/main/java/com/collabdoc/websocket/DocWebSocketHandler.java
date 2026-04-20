package com.collabdoc.websocket;

import com.collabdoc.service.DocumentService;
import com.collabdoc.service.PermissionService;
import com.collabdoc.dto.WebSocketMessage;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;  //连接关闭状态（正常关闭、异常关闭等）
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession; //WebSocket会话，代表一个客户端连接
import org.springframework.web.socket.handler.TextWebSocketHandler; //文本消息处理器基类，处理文本类型的WebSocket消息

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
 * @author xiaozhen
 */
@Component  //告诉Spring这是一个组件，spring会自动创建它的实例，其他地方可以用@Autowired注入
public class DocWebSocketHandler extends TextWebSocketHandler {

    private final RoomManager roomManager;
    private final DocumentService documentService;
    private final PermissionService permissionService;
    private final ObjectMapper mapper = new ObjectMapper(); //Jackson的JSON解析器，负责把JSON字符串 ↔ Java对象互相转换
    private final Executor webSocketExecutor;

    //构造器注入
    public DocWebSocketHandler(RoomManager roomManager, DocumentService documentService, PermissionService permissionService, Executor webSocketExecutor){
        this.roomManager = roomManager;
        this.documentService = documentService;
        this.permissionService = permissionService;
        this.webSocketExecutor = webSocketExecutor;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        //session：代表这个客户端的会话对象
        System.out.println("WebSocket 连接建立：" + session.getId());
        System.out.println("连接 URI：" + session.getUri());

        String query = session.getUri().getQuery();
        String userId = null;
        if (query != null && query.contains("userId=")){
            userId = query.split("userId=")[1].split("&")[0];
        }

        //如果取不到，打印日志，但不影响连接
        if (userId == null) userId = "anonymous";
        session.getAttributes().put("userId",userId);
        System.out.println("userId:" + userId);
    }


    //核心：处理消息
    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        //异步处理
        webSocketExecutor.execute(() -> {
            try {
                processMessage(session,message);
            }catch (Exception e){
                System.err.println("处理消息失败：" + e.getMessage());
            }
        });
    }

    /**
     * 处理文本消息
     *
     * @param session 当前会话
     * @param message 收到的消息
     * @throws Exception 处理异常
     */
    private void processMessage(WebSocketSession session,TextMessage message)throws Exception{
        String payload = message.getPayload();

        // 心跳检测
        if ("ping".equals(payload)) {
            session.sendMessage(new TextMessage("pong"));
            return;
        }

        // 使用 DTO 解析
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
            session.sendMessage(new TextMessage(replyJson));

        } else if ("update".equals(type)) {
            String docId = msg.getDocId();
            String newContent = msg.getContent();
            String userId = (String) session.getAttributes().get("userId");

            if (!permissionService.hasPermission(docId, userId, "edit")) {
                System.out.println("用户无编辑权限：" + userId);
                return;
            }

            documentService.saveContent(docId, newContent);
            roomManager.broadcastToRoom(docId, session, payload);
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        roomManager.leaveRoom(session);     //清除用户信息
        System.out.println("WebSocket 连接关闭：" + session.getId());
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        System.err.println("WebSocket 传输错误：" + exception.getMessage());
        roomManager.leaveRoom(session);     ////清除用户信息
    }

    private void printDiff(String oldStr,String newStr){
        if (oldStr == null) oldStr = "";
        if (newStr == null) newStr = "";

        if (oldStr.equals(newStr)){
            System.out.println("内容无变化");
            return;
        }

        //找出最短长度
        int minLen = Math.min(oldStr.length(),newStr.length());

        //找出第一个不同的位置
        int firstDiff = 0;
        for (int i=0;i<minLen;i++){
            if (oldStr.charAt(i) != newStr.charAt(i)){
                firstDiff = i;
                break;
            }
            firstDiff = i+1;
        }

        //判断操作类型
        if (newStr.length()>oldStr.length()){
            //新增了内容
            int addedLen = newStr.length() - oldStr.length();   //获取新增内容长度
            String added = newStr.substring(firstDiff,firstDiff+addedLen);  //获取新增内容
            System.out.println("【新增】在位置" + firstDiff + "添加了：" + added);
        } else if (newStr.length()<oldStr.length()) {
            //删除了内容
            int deletedLen = oldStr.length() - newStr.length(); //获取新增内容长度
            String deleted = oldStr.substring(firstDiff,firstDiff + deletedLen);
            System.out.println("【删除】在位置" + firstDiff + "删除了：" + deleted);
        }else {
            //长度相同，是替换
            String oldPart = oldStr.substring(firstDiff,Math.min(firstDiff + 20,oldStr.length()));
            String newPart = newStr.substring(firstDiff,Math.min(firstDiff + 20,newStr.length()));
            System.out.println("【修改】 位置" + firstDiff + "处");
            System.out.println("原内容：" + oldPart +(oldStr.length()>firstDiff + 20?"...":""));
            System.out.println("新内容：" + newPart +(newStr.length()>firstDiff + 20?"...":""));

            //打印长度变化
            System.out.println(" 长度变化：" + oldStr.length() + "--→" + newStr.length() + "字符");
        }
    }

}
