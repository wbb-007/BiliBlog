package com.blogbili.blog.model;

import java.util.List;

public record CreatorProfile(
    String name,
    String title,
    String bio,
    String avatarLabel,
    String avatarUrl,
    String bannerStyle,
    String followers,
    String likes,
    String posts,
    List<String> tags
) {
}
