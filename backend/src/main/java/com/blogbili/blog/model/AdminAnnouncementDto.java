package com.blogbili.blog.model;

public record AdminAnnouncementDto(
    Long id,
    String title,
    String content,
    boolean active,
    String createdAt
) {
}
