package com.collabdoc.service;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class LoginCacheService {

    private final StringRedisTemplate redisTemplate;

    // 缓存5分钟
    private static final int CACHE_MINUTES = 5;
    private static final String PREFIX = "login:success:";

    public LoginCacheService(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /**
     * 检查用户最近是否成功登录过（缓存是否命中）
     */
    public boolean isRecentlyLoggedIn(String username) {
        String key = PREFIX + username;
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }

    /**
     * 记录用户成功登录（写入缓存）
     */
    public void recordSuccess(String username) {
        String key = PREFIX + username;
        redisTemplate.opsForValue().set(key, "1", CACHE_MINUTES, TimeUnit.MINUTES);
    }

    /**
     * 清除用户的登录缓存（密码修改等场景用）
     */
    public void clearCache(String username) {
        String key = PREFIX + username;
        redisTemplate.delete(key);
    }
}