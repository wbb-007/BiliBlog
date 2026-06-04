package com.blogbili.blog.model;

public record AuthResponse(String message, String token, UserSessionDto user) {
}
