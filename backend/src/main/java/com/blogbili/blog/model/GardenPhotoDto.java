package com.blogbili.blog.model;

public record GardenPhotoDto(
    String title,
    String location,
    String imageUrl,
    String caption,
    String color
) {
}
