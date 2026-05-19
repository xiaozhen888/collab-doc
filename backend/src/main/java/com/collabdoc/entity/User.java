package com.collabdoc.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class User {
    private String id;
    private String username;
    private String password;
    private String email;
    private LocalDateTime createTime;
}
