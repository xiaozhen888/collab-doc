package com.collabdoc.service;

import com.collabdoc.dto.UserDTO;
import com.collabdoc.entity.User;
import com.collabdoc.mapper.UserMapper;
import com.collabdoc.utils.JwtUtil;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class UserService {
    private final UserMapper userMapper;    //用户数据访问对象，负责数据库操作
    private final JwtUtil jwtUtil;  //JWT工具类，负责生成和解析token
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();  //密码加密器，把明文密码加密成密文存储
    private final LoginCacheService loginCacheService;  //登录缓存服务，负责记录用户的登录状态，减少重复BCrypt校验

    //构造器注入
    public UserService(UserMapper userMapper, JwtUtil jwtUtil, LoginCacheService loginCacheService) {
        this.userMapper = userMapper;
        this.jwtUtil = jwtUtil;
        this.loginCacheService = loginCacheService;
    }

    //注册
    public String register(String username, String password, String email) {
        //检查用户名是否存在
        User exist = userMapper.selectList(null)    //查询所有用户
                .stream()   //转换成流（stream让代码更简洁，更易读）
                //过滤出用户名匹配的用户
                //用户名唯一，理论上只有一个，但filter返回的是流，也可能是空流
                .filter(u -> u.getUsername().equals(username))
                .findFirst()    //取第一个
                .orElse(null);  //找不到就返回null
        if (exist != null) throw new RuntimeException("用户名已存在");

        User user = new User();
        user.setId(UUID.randomUUID().toString());
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password)); //密码用BCrypt加密后存储（不存明文）
        user.setEmail(email);
        user.setCreateTime(LocalDateTime.now());
        userMapper.insert(user);

        return jwtUtil.generateToken(user.getId(), username);    //生成JWT token并返回，前端保存后用于后续请求认证
    }

    //登录
    public String login(String username, String password) {
        // 1. 先查用户是否存在
        User user = userMapper.selectByUsername(username);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }

        // 2. 检查缓存：最近5分钟内成功登录过？
        if (loginCacheService.isRecentlyLoggedIn(username)) {
            // 缓存命中，跳过BCrypt，直接生成token
            System.out.println("【登录缓存】命中用户: " + username + "，跳过密码校验");
            return jwtUtil.generateToken(user.getId(), username);
        }

        // 3. 缓存未命中，正常BCrypt校验
        System.out.println("【登录缓存】未命中用户: " + username + "，执行密码校验");
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new RuntimeException("用户名或密码错误");
        }

        // 4. 校验成功，写入缓存
        loginCacheService.recordSuccess(username);

        // 5. 生成token
        return jwtUtil.generateToken(user.getId(), username);
    }

    //搜索用户（排除自己）
    public List<UserDTO> searchUsers(String keyword, String currentUserId) {
        return userMapper.selectList(null).stream()
                .filter(u -> !u.getId().equals(currentUserId))
                .filter(u -> u.getUsername().toLowerCase().contains(keyword.toLowerCase()))
                .map(u -> {
                    UserDTO dto = new UserDTO();
                    dto.setId(u.getId());
                    dto.setUsername(u.getUsername());
                    dto.setEmail(u.getEmail());
                    return dto;
                })
                .limit(10)  //最多返回10条
                .toList();
    }

    public User getByUsername(String username) {
        return userMapper.selectList(null).stream()
                .filter(u -> u.getUsername().equals(username))
                .findFirst()
                .orElse(null);
    }

    //根据ID 获取用户
    public User getById(String id) {
        return userMapper.selectById(id);
    }


    public UserDTO getUserDTO(String userId) {
        User user = getById(userId);
        if (user == null) return null;

        UserDTO dto = new UserDTO();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setCreateTime(user.getCreateTime());
        return dto;
    }
}

