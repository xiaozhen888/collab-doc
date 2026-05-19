package com.collabdoc.config;

import com.collabdoc.entity.User;
import com.collabdoc.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 密码初始化处理器
 *
 * 作用：项目启动时自动检测并加密数据库中的明文密码
 *
 * 背景：
 *   - init-data.sql 中用户密码以明文存储（方便阅读和修改）
 *   - 项目启动时，该组件自动将明文密码转换为 BCrypt 密文
 *   - 避免每次更新 init.sql 时手动生成不同的 BCrypt 密文
 *
 * 执行逻辑：
 *   1. 遍历 user 表中所有用户
 *   2. 判断密码是否为明文（长度 < 20 且不以 "$2a$" 开头）
 *   3. 若是明文，使用 BCryptPasswordEncoder 加密并更新数据库
 *   4. 已是密文则跳过，避免重复加密
 *
 * 使用场景：
 *   - 首次部署：init.sql 插入明文 → 启动自动加密
 *   - 重新初始化：删表重建后再次自动加密
 *   - 日常启动：检测到密文，直接跳过，无额外开销
 *
 * @author xiaozhen
 * @since 2026-04-24
 */
@Component  // Spring 组件，启动时自动加载
public class PasswordInitRunner implements CommandLineRunner {

    @Autowired
    private UserMapper userMapper;  // 用户数据访问对象，用于查询和更新用户密码

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();  // BCrypt 密码加密器

    /**
     * 项目启动后执行密码加密检查
     *
     * @param args 命令行参数（未使用）
     */
    @Override
    public void run(String... args) {
        // 查询所有用户
        List<User> users = userMapper.selectList(null);
        int count = 0;  // 记录加密用户数量

        // 遍历每个用户，检查密码状态
        for (User user : users) {
            String pwd = user.getPassword();

            // 判断是否为明文密码：
            // 1. 长度小于 20（BCrypt 密文通常 60 位左右）
            // 2. 不以 "$2a$" 开头（BCrypt 密文的固定前缀）
            if (pwd != null && pwd.length() < 20 && !pwd.startsWith("$2a$")) {
                // 明文 → 加密
                String encrypted = passwordEncoder.encode(pwd);
                user.setPassword(encrypted);
                userMapper.updateById(user);
                count++;
                System.out.println("✅ 用户 " + user.getUsername() + " 密码已自动加密");
            }
        }

        // 输出加密结果日志
        if (count > 0) {
            System.out.println("🔐 共加密 " + count + " 个用户的密码");
        } else {
            System.out.println("🔐 所有用户密码已是密文，无需加密");
        }
    }
}