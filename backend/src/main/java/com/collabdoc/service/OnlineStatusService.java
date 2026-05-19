package com.collabdoc.service;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class OnlineStatusService {
    private final RedisTemplate<String,String> redisTemplate;

    public OnlineStatusService(RedisTemplate<String,String> redisTemplate){
        this.redisTemplate = redisTemplate;
    }

    //用户上线
    public void userOnline(String userId,String docId){
        String key = "online:user:" + userId;
        redisTemplate.opsForValue()     //获取字符串操作对象
                .set(key,docId,30, TimeUnit.MINUTES);   //设置键值对，并设置过期时间
                //30分钟自动过期，防止用户异常退出导致数据残留
    }

    //用户下线
    public void userOffline(String userId){
        String key = "online:user:" + userId;
        redisTemplate.delete(key);      //删掉redis中的key,用户下线
    }

    //检查用户是否在线
    public boolean isOnline(String userId){
        String key = "online:user:" + userId;
       return Boolean.TRUE.equals(redisTemplate.hasKey(key));   //安全判断，防止返回null时出现空指针
    }

    //获取用户所在的文档
    //获取用户所在文档的ID，如果用户不在线就返回null
    public String getUserDoc(String userId){
        String key = "online:user:" + userId;
        return redisTemplate.opsForValue().get(key);
    }

    //获取文档的在线用户数
    public long getDocOnlineCount(String docId){
        String key = "online:user:" + docId;
        return redisTemplate.opsForSet()    //获取集合操作对象
                .size(key);     //获取集合的大小（在线用户数）
    }
}
