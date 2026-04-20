package com.collabdoc.utils;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class JwtUtilTest {

    @Autowired
    private JwtUtil jwtUtil;

    @Test
    void testGenerateAndParseToken(){
        //1.生成token
        String userId = "test-user-123";
        String username = "testuser";
        String token = jwtUtil.generateToken(userId,username);

        //2.验证token不为空
        assertNotNull(token);

        //3.解析token
        String parsedUserId = jwtUtil.getUserId(token);

        //4.验证解析结果正确
        assertEquals(userId,parsedUserId);
    }

    @Test
    void testInvalidToken(){
        //测试无效token
        String invalidToken = "invalid.token.here";

        assertThrows(Exception.class,() -> {
            jwtUtil.getUserId(invalidToken);
        });
    }
}
