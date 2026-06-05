package com.blogbili.blog.model;

public record AdminAlbumPhotoDto(
    Long id,
    String title,
    String location,
    String imageUrl,
    String caption,
    String color,
    boolean active,
    String createdAt
) {
}
