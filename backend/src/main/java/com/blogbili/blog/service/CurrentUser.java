package com.blogbili.blog.service;

import com.blogbili.blog.entity.UserEntity;

public record CurrentUser(Long id, String email, String nickname, UserEntity.Role role) {
}
