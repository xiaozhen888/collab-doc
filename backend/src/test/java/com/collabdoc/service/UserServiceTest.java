package com.collabdoc.service;

import com.collabdoc.entity.User;
import com.collabdoc.mapper.UserMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class UserServiceTest {

    @Autowired
    private UserService userService;

    @Autowired
    private UserMapper userMapper;

    @Test
    void testRegisterAndLogin(){
        String username = "unittest_" + System.currentTimeMillis();
        String password = "test123";
        String email = "test@test.com";

        //1.注册
        String token = userService.register(username,password,email);
        assertNotNull(token);

        //2.登录
        String loginToken = userService.login(username,password);
        assertNotNull(loginToken);

        //3.验证用户已存入数据库
        User user = userMapper.selectList(null).stream()
                .filter(u -> u.getUsername().equals(username))
                .findFirst()
                .orElse(null);
        assertNotNull(user);
        assertEquals(username,user.getUsername());
    }

    @Test
    void testLoginWithWrongPassword(){
        String username = "test_login_user1";
        String password = "correct123";
        userService.register(username,password,"test@test.com");

        //用错误密码登录应该抛异常
        assertThrows(RuntimeException.class,() -> {
            userService.login(username,"wrongpassword");
        });
    }

}
