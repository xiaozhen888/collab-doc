package com.collabdoc.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig implements WebMvcConfigurer {

    //配置跨域映射
    //CorsRegistry registry：注册器，用来添加跨域规则
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins("http://localhost:5173")    //允许哪些前端域名访问    http://localhost:5173：允许Vue开发服务器的地址
                .allowedMethods("*")    //允许哪些HTTP方法
                .allowedHeaders("*")    //允许哪些请求头
                .allowCredentials(true);
    }
}
