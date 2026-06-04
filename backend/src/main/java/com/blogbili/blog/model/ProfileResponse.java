package com.blogbili.blog.model;

import java.util.List;

public record ProfileResponse(
    ProfileIdentity profile,
    List<PostSummary> pinnedPosts,
    List<PostSummary> recentPosts,
    List<TimelineItem> timeline,
    List<Metric> favoriteBoards,
    List<Metric> growthStats
) {
}
