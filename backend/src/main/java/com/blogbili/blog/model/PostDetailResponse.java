package com.blogbili.blog.model;

import java.util.List;

public record PostDetailResponse(
    DetailedPost post,
    List<CommentDto> comments,
    List<PostSummary> relatedPosts
) {
}
