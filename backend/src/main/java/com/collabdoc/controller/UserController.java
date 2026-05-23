package com.collabdoc.controller;

import com.collabdoc.dto.UserDTO;
import com.collabdoc.request.LoginRequest;
import com.collabdoc.service.LoginRateLimiterService;
import com.collabdoc.service.UserService;
import com.collabdoc.utils.JwtUtil;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/user")    //设置路径前缀
public class UserController {

    private final UserService userService;
    private final JwtUtil jwtUtil;
    private final LoginRateLimiterService rateLimiter;

    public UserController (UserService userService, JwtUtil jwtUtil, LoginRateLimiterService rateLimiter){
        this.userService = userService;
        this.jwtUtil = jwtUtil;
        this.rateLimiter = rateLimiter;
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
    public ResponseEntity<?> login(@RequestBody LoginRequest request, HttpServletRequest httpRequest) {
        String ip = getClientIp(httpRequest);
        String username = request.getUsername();

        // 第一层：限流检查
        if (!rateLimiter.allowLogin(username, ip)) {
            return ResponseEntity.status(429)
                    .body(Map.of(
                            "code", 429,
                            "message", "登录尝试次数过多，请1分钟后再试",
                            "remaining", 0
                    ));
        }

        // 正常登录流程
        try {
            String token = userService.login(username, request.getPassword());
            return ResponseEntity.ok(Map.of("token", token));
        } catch (RuntimeException e) {
            // 密码错误或用户不存在
            int remaining = rateLimiter.getRemainingAttempts(username, ip);
            return ResponseEntity.badRequest()
                    .body(Map.of(
                            "code", 400,
                            "message", e.getMessage(),
                            "remaining", remaining
                    ));
        }
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

    /**
     * 获取客户端真实IP
     */
    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty()) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty()) {
            ip = request.getRemoteAddr();
        }
        // 如果有多个IP，取第一个
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip != null ? ip : "unknown";
    }

}
