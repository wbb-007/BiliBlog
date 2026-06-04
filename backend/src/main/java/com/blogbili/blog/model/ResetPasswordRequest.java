package com.blogbili.blog.model;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record ResetPasswordRequest(
    @NotBlank(message = "email is required")
    @Email(message = "email format is invalid")
    String email,
    @NotBlank(message = "code is required")
    String code,
    @NotBlank(message = "password is required")
    String password
) {
}
