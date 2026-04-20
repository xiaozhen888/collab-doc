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

    //构造器注入
    public UserService(UserMapper userMapper, JwtUtil jwtUtil) {
        this.userMapper = userMapper;
        this.jwtUtil = jwtUtil;
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
        User user = userMapper.selectList(null).stream()
                .filter(u -> u.getUsername().equals(username))
                .findFirst()
                .orElse(null);

        if (user == null) throw new RuntimeException("用户不存在");

        //passwordEncoder.matches(明文，密文)：比对输入的密码和数据库中的加密密码
        if (!passwordEncoder.matches(password, user.getPassword())) throw new RuntimeException("用户名或密码错误");

        //匹配成功，生成token返回给前端浏览器
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

