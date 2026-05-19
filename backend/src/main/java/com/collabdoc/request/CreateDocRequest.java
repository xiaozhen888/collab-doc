package com.collabdoc.request;

import lombok.Data;

@Data
public class CreateDocRequest {
    private String title;
    private String content;
}
