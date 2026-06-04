package com.blogbili.blog.model;

import jakarta.validation.constraints.NotBlank;
import java.util.List;

public record AdminPostUpdateRequest(
    @NotBlank(message = "title is required")
    String title,
    @NotBlank(message = "excerpt is required")
    String excerpt,
    @NotBlank(message = "category is required")
    String category,
    List<String> tags,
    @NotBlank(message = "content is required")
    String content,
    String coverTone
) {
}
