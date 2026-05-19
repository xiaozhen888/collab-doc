package com.collabdoc.dto;

import lombok.Data;

@Data
public class WebSocketMessage {
    private String type;        //消息类型
    // join（用户加入房间）,
    // update（用户更新内容）,
    // init（服务器告诉新用户当前文档内容）,
    // presence（通知房间里所有人当前在线人数）
    private String docId;       //文档ID
    private String content;     //文档内容
    private String userId;      //用户ID
    private Integer onlineCount;//在线人数
}
