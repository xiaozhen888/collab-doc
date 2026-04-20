package com.collabdoc.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class Document {
    private String id;          //文档id
    private String content;     //文档内容
    private String title;       //文档标题
    private String ownerId;     //创建者id
    private LocalDateTime createTime;//创建时间
    private LocalDateTime updateTime;//更新时间
}
