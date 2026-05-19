package com.collabdoc.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

/**
 * JWT 工具类
 *
 * 职责：
 * 1. 生成 JWT token
 * 2. 解析 JWT token
 * 3. 从 token 中提取用户 ID
 */
@Component
public class JwtUtil {
    //固定密钥
    private static final String SECRET = "mySecretKeyForJwtTokenGeneration2024VeryLongAndSecure";
    //KEY：密钥类型
    //Keys.secretKeyFor(...)：生成一个安全的随机密钥
    //SignatureAlgorithm.HS256：使用 HMAC-SHA256 算法
    //每次重启服务器密钥都会变化，生产环境应该固定密钥
    private static final Key KEY = Keys.hmacShaKeyFor(SECRET.getBytes());
    //token的有效期
    private static final long EXPIRATION = 86400000;    //24小时

    /**
     * 生成 JWT token
     *
     * @param userId 用户 ID
     * @param username 用户名
     * @return JWT token 字符串
     */
    public String generateToken(String userId, String username) {
        return Jwts.builder()   //开始构建JWT
                .setSubject(userId)     //设置主题，通常存放用户唯一标识
                .claim("username", username)     //添加自定义字段，键是“username”,值是用户名
                .setIssuedAt(new Date())        //设置签发时间为当前时间
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION))   //设置过期时间=当前时间+token的有效期
                .signWith(KEY)      //用密钥对JWT进行签名，防止篡改
                .compact();         //将JWT压缩成最终的字符串格式
    }

    //验证token
    public Claims parseToken(String token) {     //返回Claims对象（包含token中的所有数据）
        // 如果 token 以 "Bearer " 开头，去掉它
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        System.out.println("JwtUtil.parseToken 收到清理后的 token: [" + token + "]");
        try {
            return Jwts.parserBuilder()     //创建JWT解析器构建器
                    .setSigningKey(KEY) //设置验证签名用的密钥（必须和生成时用的密钥相同）
                    .build()    //构建解析器
                    .parseClaimsJws(token)      //解析token，验证签名。如果签名无效或token过期，会抛出异常
                    .getBody();     //获取token中的负载部分，即claims对象（包含：subject(用户ID)、username(用户名)、iat(签发时间)、exp(过期时间)）
        } catch (Exception e) {
            System.out.println("JwtUtil 解析异常: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    //获取用户ID
    public String getUserId(String token) {
        return parseToken(token)    //解析token，得到Claims
                .getSubject();  //从Claims中取出subject(用户ID)
    }
}
