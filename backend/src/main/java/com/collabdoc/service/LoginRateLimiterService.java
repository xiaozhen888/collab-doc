package com.collabdoc.service;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class LoginRateLimiterService {

    private final StringRedisTemplate redisTemplate;

    // 1分钟内最多5次尝试
    private static final int MAX_ATTEMPTS = 5;
    private static final int WINDOW_SECONDS = 60;
    private static final String PREFIX = "login:limit:";

    public LoginRateLimiterService(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /**
     * 检查是否允许登录
     * @param username 用户名
     * @param ip 客户端IP
     * @return true 允许登录，false 超过限制
     */
    public boolean allowLogin(String username, String ip) {
        String key = PREFIX + username + ":" + ip;

        Long attempts = redisTemplate.opsForValue().increment(key);

        if (attempts == null) {
            // Redis 异常，保守起见允许登录（或根据策略拒绝）
            return true;
        }

        if (attempts == 1) {
            // 第一次尝试，设置过期时间
            redisTemplate.expire(key, WINDOW_SECONDS, TimeUnit.SECONDS);
        }

        return attempts <= MAX_ATTEMPTS;
    }

    /**
     * 获取剩余可尝试次数
     */
    public int getRemainingAttempts(String username, String ip) {
        String key = PREFIX + username + ":" + ip;
        String value = redisTemplate.opsForValue().get(key);
        if (value == null) {
            return MAX_ATTEMPTS;
        }
        int attempts = Integer.parseInt(value);
        return Math.max(0, MAX_ATTEMPTS - attempts);
    }
}