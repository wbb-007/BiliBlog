package com.blogbili.blog.model;

import java.util.List;

public record HomeResponse(
    java.util.List<CategoryDto> categories,
    PostSummary featuredPost,
    List<PostSummary> spotlight,
    List<PostSummary> posts,
    CreatorProfile creator,
    List<Announcement> announcements,
    List<Metric> trends
) {
}
