package com.collabdoc.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ShareLink {
    private String id;
    private String docId;
    private String shareCode;   //8位分享码（短码）
    private String permission;  //权限 read or edit
    private String createdBy;   //创建者
    private LocalDateTime createTime;
    private LocalDateTime expireTime;   //过期时间
}
