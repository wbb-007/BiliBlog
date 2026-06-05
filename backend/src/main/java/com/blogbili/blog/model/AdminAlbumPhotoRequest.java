package com.blogbili.blog.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AdminAlbumPhotoRequest(
    @NotBlank(message = "title is required")
    @Size(max = 160, message = "title is too long")
    String title,
    @NotBlank(message = "location is required")
    @Size(max = 120, message = "location is too long")
    String location,
    @NotBlank(message = "imageUrl is required")
    @Size(max = 1000, message = "imageUrl is too long")
    String imageUrl,
    @NotBlank(message = "caption is required")
    @Size(max = 600, message = "caption is too long")
    String caption,
    @Size(max = 255, message = "color is too long")
    String color,
    boolean active
) {
}
