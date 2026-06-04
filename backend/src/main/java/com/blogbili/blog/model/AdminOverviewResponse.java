package com.blogbili.blog.model;

import java.util.List;

public record AdminOverviewResponse(
    List<Metric> metrics,
    List<PostSummary> latestPosts,
    List<UserSessionDto> latestUsers,
    List<AdminAnnouncementDto> announcements
) {
}
