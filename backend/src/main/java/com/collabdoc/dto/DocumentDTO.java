package com.collabdoc.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class DocumentDTO {
    private String id;
    private String title;
    private String content;
    private String ownerId;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
