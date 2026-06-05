package com.blogbili.blog.model;

public record LatestCommentDto(
    Long id,
    Long postId,
    String postTitle,
    String author,
    String authorInitial,
    String authorAvatarUrl,
    String content,
    String time
) {
}
