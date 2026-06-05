package com.blogbili.blog.model;

public record GardenLinkDto(
    String name,
    String description,
    String url,
    String avatarUrl,
    String tag
) {
}
