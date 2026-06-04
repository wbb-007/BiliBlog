package com.blogbili.blog.model;

import java.util.List;

public record CommunityResponse(
    List<PostSummary> posts,
    List<Metric> metrics
) {
}
