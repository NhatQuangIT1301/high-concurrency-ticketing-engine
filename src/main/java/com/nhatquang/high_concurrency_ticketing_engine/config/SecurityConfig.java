package com.nhatquang.high_concurrency_ticketing_engine.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable) // Tắt bảo vệ CSRF để Postman có thể gọi POST
            .authorizeHttpRequests(auth -> auth
                .anyRequest().permitAll() // Cho phép toàn bộ API đi qua không cần đăng nhập
            );
        return http.build();
    }
}