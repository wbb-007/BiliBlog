package com.blogbili.blog.model;

public record CommentDto(
    Long id,
    String author,
    String authorInitial,
    String time,
    String content
) {
}
