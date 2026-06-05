package com.blogbili.blog.model;

public record GardenNoteDto(
    String title,
    String content,
    String mood,
    String time,
    String tone
) {
}
