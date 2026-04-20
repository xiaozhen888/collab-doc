package com.collabdoc.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@Configuration
public class ThreadPoolConfig {

    @Bean
    public Executor webSocketExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(10);   //核心线程数：10个线程一直存活，即使空闲也不销毁
        executor.setMaxPoolSize(50);    //最大线程数：任务多时可以扩展到50个线程
        executor.setQueueCapacity(100);     //队列容量：当核心线程都在忙时，新任务会进入队列等待，最多存100个
        executor.setThreadNamePrefix("ws- ");   //线程名前缀
        executor.initialize();  //初始化线程
        return executor;
    }
}
