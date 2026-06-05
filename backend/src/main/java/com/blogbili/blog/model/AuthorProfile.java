package com.blogbili.blog.model;

public record AuthorProfile(
    String name,
    String title,
    String avatarLabel,
    String avatarUrl,
    String followers,
    String articles
) {
}
