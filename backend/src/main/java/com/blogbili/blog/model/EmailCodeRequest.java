package com.blogbili.blog.model;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record EmailCodeRequest(
    @NotBlank(message = "email is required")
    @Email(message = "email format is invalid")
    String email,
    @NotBlank(message = "purpose is required")
    String purpose
) {
}
