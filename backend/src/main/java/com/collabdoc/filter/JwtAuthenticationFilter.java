package com.collabdoc.filter;

import com.collabdoc.utils.JwtUtil;
import io.jsonwebtoken.Claims;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;

//声明一个公共类，继承OncePerRequestFilter，保证每个请求只经过一次（防止内部转发时多次执行，浪费性能）
//不能删，删掉后端就不会验证token，任何人都能直接访问需要认证的接口
//作用：验证每个请求中的JWT token，判断用户是否已经登录
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtUtil jwtUtil;

    public JwtAuthenticationFilter(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    //核心过滤方法
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        //获取并验证token
        String header = request.getHeader("Authorization");     //从HTTP请求头中获取Authorization字段
        //检查 Authorization 头是否存在，并且是否以 Bearer 开头。
        //Bearer 是 HTTP 认证中规定的标准格式，区分认证类型
        //如果不以 Bearer 开头，说明不是标准 token，直接跳过认证
        if (header != null && header.startsWith("Bearer ")) {
            //找到第一个空格后面的位置
            int start = header.indexOf(" ") + 1;
            //跳过可能存在的空格
            while (start < header.length() && header.charAt(start) == ' ') start++;
            String token = header.substring(7);         //截取 token 部分。"Bearer " 长度为 7，substring(7) 从第 7 个字符开始取，得到纯 token。
            try {
                Claims claims = jwtUtil.parseToken(token);      //尝试用JwtUtil解析token，如果token无效或过期，会抛出异常
                String userId = claims.getSubject();
                //将用户信息存入Security上下文
                //创建Spring Security的认证令牌：
                //userId:用户身份，null:凭证（密码，这里不需要），new ArrayList<>():权限列表（空，暂时不需要）
                UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(userId, null, new ArrayList<>());
                SecurityContextHolder.getContext().setAuthentication(auth);

                //把userId存入request属性，供websocket使用
                request.setAttribute("userId", userId);
                System.out.println("JWT 认证成功：userId=" + userId);
            } catch (Exception e) {
                System.out.println("JWT 认证失败：" + e.getMessage());
            }
        }
        //交给下一个过滤器或Controller处理
        //无论token验证成功还是失败，都要调用这个方法，否则请求会被卡住
        filterChain.doFilter(request, response);
    }

