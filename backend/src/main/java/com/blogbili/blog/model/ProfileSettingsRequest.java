package com.blogbili.blog.model;

import java.util.List;

public record ProfileSettingsRequest(
    String name,
    String headline,
    String bio,
    String avatarUrl,
    String avatarLabel,
    String bannerStyle,
    String followers,
    String likes,
    List<String> tags
) {
}
