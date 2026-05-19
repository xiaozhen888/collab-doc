package com.collabdoc.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

//告诉Spring:这是一个全局异常处理器，会拦截所有Controller中抛出的异常
//自动将返回值转为JSON格式
//作用范围：整个应用中所有@RestController抛出的异常都会被这里处理
@RestControllerAdvice
public class GlobalExceptionHandler {

    //ExceptionHandler：标记这个方法专门处理某种异常
    //RuntimeException.class：指定处理运行时异常及其子类
    @ExceptionHandler(RuntimeException.class)
    //ResponseStatus：指定返回给客户端的HTTP状态码
    //HttpStatus.BAD_GATEWAY：状态码502
    //HttpStatus.BAD_REQUEST：状态码400
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String,String> handleRuntimeException(RuntimeException e){
        Map<String,String> error = new HashMap<>();
        error.put("message",e.getMessage());    //从异常对象中取出错误消息
        return error;
    }
}
