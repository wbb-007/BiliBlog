package com.blogbili.blog.controller;

import com.blogbili.blog.model.AuthResponse;
import com.blogbili.blog.model.EmailCodeRequest;
import com.blogbili.blog.model.LoginRequest;
import com.blogbili.blog.model.RegisterRequest;
import com.blogbili.blog.model.ResetPasswordRequest;
import com.blogbili.blog.model.SendCodeResponse;
import com.blogbili.blog.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/send-code")
    public SendCodeResponse sendCode(@Valid @RequestBody EmailCodeRequest request) {
        return authService.sendCode(request);
    }

    @PostMapping("/register")
    public AuthResponse register(@Valid @RequestBody RegisterRequest request) {
        return authService.register(request);
    }

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }

    @PostMapping("/reset-password")
    public AuthResponse resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        return authService.resetPassword(request);
    }

    @GetMapping("/me")
    public AuthResponse me(HttpServletRequest request) {
        return authService.me(request);
    }

    @PostMapping("/logout")
    public void logout(HttpServletRequest request) {
        authService.logout(request);
    }
}
