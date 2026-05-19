package com.collabdoc.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UserDTO {
    private String id;
    private String username;
    private String email;
    private LocalDateTime createTime;

}
