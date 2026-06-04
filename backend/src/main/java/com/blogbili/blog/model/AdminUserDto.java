package com.blogbili.blog.model;

public record AdminUserDto(
    Long id,
    String email,
    String nickname,
    String role,
    String status,
    String createdAt,
    String lastLoginAt
) {
}
