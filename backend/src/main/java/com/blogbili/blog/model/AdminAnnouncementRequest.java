package com.blogbili.blog.model;

import jakarta.validation.constraints.NotBlank;

public record AdminAnnouncementRequest(
    @NotBlank(message = "title is required")
    String title,
    @NotBlank(message = "content is required")
    String content,
    boolean active
) {
}
