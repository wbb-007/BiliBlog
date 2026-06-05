package com.blogbili.blog.model;

import java.util.List;

public record ProfileIdentity(
    String name,
    String headline,
    String bio,
    String avatarLabel,
    String avatarUrl,
    String bannerStyle,
    List<Metric> stats,
    List<String> tags,
    String lastUpdated
) {
}
