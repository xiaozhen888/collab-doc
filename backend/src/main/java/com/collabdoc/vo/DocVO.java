package com.collabdoc.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class DocVO {
    private String id;
    private String title;
    private String content;
    private String ownerId;
    private String ownerName;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private boolean canEdit;
    private boolean canManage;
}
