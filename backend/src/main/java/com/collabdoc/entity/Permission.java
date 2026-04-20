package com.collabdoc.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class Permission {
    private String id;
    private String docId;
    private String userId;
    private String permission;  //read,edit,manage
    private LocalDateTime createTime;
}
