package com.nhatquang.high_concurrency_ticketing_engine.service;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.nhatquang.high_concurrency_ticketing_engine.dto.AuthResponse;
import com.nhatquang.high_concurrency_ticketing_engine.dto.LoginRequest;
import com.nhatquang.high_concurrency_ticketing_engine.dto.RegisterRequest;
import com.nhatquang.high_concurrency_ticketing_engine.entity.User;
import com.nhatquang.high_concurrency_ticketing_engine.enums.Role;
import com.nhatquang.high_concurrency_ticketing_engine.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    // --- XỬ LÝ ĐĂNG KÝ ---
    public AuthResponse register(RegisterRequest request) {
        // Kiểm tra email trùng lặp
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email này đã được sử dụng!");
        }

        // Tạo User mới và mã hóa mật khẩu
        User user = User.builder()
        .username(request.getUsername())
        .email(request.getEmail())
        .password(passwordEncoder.encode(request.getPassword()))// Băm password
        .role(Role.USER)// Mặc định là USER
        .build();

        // Lưu xuống PostgreSQL
        userRepository.save(user);

        // Tạo Token cho User vừa đăng ký
        String jwtToken = jwtService.generateToken(user);

        return AuthResponse.builder().token(jwtToken).message("Đăng ký thành công!").build();
    }

    // --- XỬ LÝ ĐĂNG NHẬP ---
    public AuthResponse authenticate(LoginRequest request) {
        // Trình quản lý của Spring Security sẽ tự động kiểm tra email và password đã băm
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));

        // Nếu code chạy đến đây nghĩa là pass/mật khẩu đúng -> Lấy thông tin User ra
        User user = userRepository.findByEmail(request.getEmail()).orElseThrow();

        // Phát sinh Token mới
        String jwtToken = jwtService.generateToken(user);

        return AuthResponse.builder().token(jwtToken).message("Đăng nhập thành công!").build();
    }
}
