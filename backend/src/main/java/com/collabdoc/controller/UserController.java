package com.collabdoc.controller;

import com.collabdoc.dto.UserDTO;
import com.collabdoc.request.LoginRequest;
import com.collabdoc.service.UserService;
import com.collabdoc.utils.JwtUtil;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/user")    //设置路径前缀
public class UserController {

    private final UserService userService;
    private final JwtUtil jwtUtil;

    public UserController (UserService userService, JwtUtil jwtUtil){
        this.userService = userService;
        this.jwtUtil = jwtUtil;
    }

    //登录和注册方法的返回类型都是Map<String,String>，因为他们都要告诉前端这个字符串是token（前端需要知道，所以用字段名token包装一下）
    @PostMapping("/register")
    public Map<String,String> register(@RequestBody LoginRequest request){   //从HTTP请求的body中读取JSON数据，转成Map
        String token = userService.register(
                request.getUsername(),
                request.getPassword(),
                null
        );
        return Map.of("token",token);
    }

    @PostMapping("/login")
    public Map<String,String> login(@RequestBody LoginRequest request){
        String token = userService.login(
                request.getUsername(),
                request.getPassword()
        );

        //获取用户信息
        com.collabdoc.entity.User user = userService.getByUsername(request.getUsername());

        Map<String,String> result = new HashMap<>();
        result.put("token",token);
        result.put("userId",user.getId());
        return result;
    }

    //搜索用户（用于添加协作者）
    @GetMapping("/search")
    public List<UserDTO> searchUsers(@RequestParam String keyword,@RequestHeader("Authorization") String authHeader){
        String token = authHeader.substring(7);
        String currentUserId = jwtUtil.getUserId(token);
        return userService.searchUsers(keyword,currentUserId);
    }
    @GetMapping("/{userId}")
    public UserDTO getUser(@PathVariable String userId){
        return userService.getUserDTO(userId);
    }

}
