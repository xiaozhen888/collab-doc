package com.collabdoc.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class DocHistory {
    private String id;  //版本记录的唯一标识
    private String docId;   //关联的文档ID
    private String content;     //该版本的文档完整内容
    private Integer version;    //版本号

    @TableField("created_by")   //指定数据库列名
    private String createBy;    //创建该版本的用户ID

    private LocalDateTime createTime;   //版本创建时间
}
