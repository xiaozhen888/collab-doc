package com.collabdoc.config;

import com.collabdoc.filter.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    //SecurityFilterChain：安全过滤器链，Spring Security用它来处理请求
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // 注册、登录公开
                        .antMatchers("/api/user/register", "/api/user/login").permitAll()
                        // 公开文档查询（分享链接用）
                        .antMatchers("/api/document/public/**").permitAll()
                        // WebSocket 握手（权限在 Handler 里校验）
                        .antMatchers("/collab/**").permitAll()
                        // 其他所有请求必须认证
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration(); //创建CORS配置对象
        config.setAllowedOrigins(List.of("http://localhost:5173")); //允许前端域名http://localhost:5173访问
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));   //允许这些HTTP方法
        config.setAllowedHeaders(List.of("*")); //允许所有请求头
        config.setAllowCredentials(true);   //允许携带凭证（token）

        //创建基于URL的CORS配置源
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        //所有路径应用这个CORS配置
        source.registerCorsConfiguration("/**", config);
        //返回配置源
        return source;
    }
}
