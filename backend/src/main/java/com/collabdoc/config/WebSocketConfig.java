package com.collabdoc.config;

import com.collabdoc.websocket.DocWebSocketHandler;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket    //开启WebSocket功能
public class WebSocketConfig implements WebSocketConfigurer {

    private final DocWebSocketHandler docWebSocketHandler;//我需要一个handler

    //构造器注入
    //spring创建WebSocketConfig时，会把DocWebSocketHandler传进来 //spring给我一个handler
    public WebSocketConfig(DocWebSocketHandler docWebSocketHandler) {
        this.docWebSocketHandler = docWebSocketHandler;
    }

    //配置websocket的路由规则（哪个URL用哪个Handler处理）
    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        //registry：注册器，用来添加websocket处理器
        //addHandler（处理器，路径）：把处理器绑定到某个URL路径
        //当客户端连接 ws://localhost:8080/collab 时，使用 docWebSocketHandler 来处理这个连接
        registry.addHandler(docWebSocketHandler, "/collab")  // /collab：websocket连接的端点地址（前端会连接 ws://localhost:8080/collab）
                .setAllowedOrigins("*");    //允许任何域名访问（开发环境用）
    }

}
