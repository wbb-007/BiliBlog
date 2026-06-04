package com.blogbili.blog.model;

import jakarta.validation.constraints.NotBlank;

public record AdminUserUpdateRequest(
    @NotBlank(message = "role is required")
    String role,
    @NotBlank(message = "status is required")
    String status
) {
}
