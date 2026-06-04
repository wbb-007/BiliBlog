package com.blogbili.blog.model;

import java.util.List;

public record DetailedPost(
    Long id,
    String title,
    String excerpt,
    String category,
    String board,
    String publishedAt,
    String views,
    String comments,
    String likes,
    String readTime,
    String coverLabel,
    String coverStyle,
    List<String> tags,
    String intro,
    AuthorProfile author,
    List<ContentBlock> blocks,
    PostStats stats
) {
}
