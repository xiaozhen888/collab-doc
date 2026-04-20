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
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))  //.cors(...)：开启CORS配置   //configurationSource(...):调用下面定义的CORS配置源
                .csrf(csrf -> csrf.disable())   //禁用CSRF，CSRF是跨站请求伪造攻击，为什么禁用：因为使用JWT token认证，不需要CSRF保护
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))   //设置Session为无状态(JWT 认证是无状态 的，不需要服务器存储Session     //SessionCreationPolicy.STATELESS：不设置Session
                .authorizeHttpRequests(auth -> auth
//                        .antMatchers("/api/user/**","/collab/**","/api/history/**","/api/export/**","/api/share/**","/api/document/public/**","/api/document/**").permitAll()
                                .anyRequest().permitAll()  //除了上面放行的路径之外的所有请求
//                        .authenticated()   //必须认证（有token且有效）
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);  //在Spring Security的认证之前，先用我配置的JWT过滤器验证token
        return http.build();    //返回构建结果
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
