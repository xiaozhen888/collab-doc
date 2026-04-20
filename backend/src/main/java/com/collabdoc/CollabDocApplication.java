package com.collabdoc;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.collabdoc.mapper")
public class CollabDocApplication {
    public static void main(String[] args){
        SpringApplication.run(CollabDocApplication.class,args);
    }
}
