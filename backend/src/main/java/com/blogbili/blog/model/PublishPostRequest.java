package com.blogbili.blog.model;

import jakarta.validation.constraints.NotBlank;
import java.util.List;

public record PublishPostRequest(
    @NotBlank(message = "title is required")
    String title,
    @NotBlank(message = "category is required")
    String category,
    List<String> tags,
    @NotBlank(message = "summary is required")
    String summary,
    @NotBlank(message = "content is required")
    String content,
    String coverTone
) {
}
