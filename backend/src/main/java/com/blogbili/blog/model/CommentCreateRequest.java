package com.blogbili.blog.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CommentCreateRequest(
    @NotBlank(message = "nickname is required")
    @Size(max = 40, message = "nickname is too long")
    String nickname,
    @NotBlank(message = "content is required")
    @Size(max = 1000, message = "content is too long")
    String content
) {
}
