package com.nhatquang.high_concurrency_ticketing_engine.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.nhatquang.high_concurrency_ticketing_engine.dto.AuthResponse;
import com.nhatquang.high_concurrency_ticketing_engine.dto.LoginRequest;
import com.nhatquang.high_concurrency_ticketing_engine.dto.RegisterRequest;
import com.nhatquang.high_concurrency_ticketing_engine.service.AuthService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
//Annotation @Valid sẽ tự động kích hoạt các ràng buộc @NotBlank, @Email, @Size đã khai báo trong DTO
public class AuthController {
    
    private final AuthService authService;

    // API: POST http://localhost:8080/api/auth/register
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.ok(authService.register(request));
    }

    // API: POST http://localhost:8080/api/auth/login
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> authenticate(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.authenticate(request));
    }
}
