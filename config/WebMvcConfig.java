package com.example.snowball.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")  // 只对 API 接口放开跨域
                .allowedOrigins("http://localhost:5173")  // 前端地址，多个可逗号分隔
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")  // 必须包含 OPTIONS
                .allowedHeaders("*")
                .allowCredentials(true)  // 如果需要带 Cookie/JWT
                .maxAge(3600);           // 预检缓存1小时，减少 OPTIONS 次数
    }
}
