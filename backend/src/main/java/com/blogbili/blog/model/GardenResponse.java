package com.blogbili.blog.model;

import java.util.List;

public record GardenResponse(
    List<Metric> metrics,
    List<GardenNoteDto> notes,
    List<GardenLinkDto> friends,
    List<GardenToolDto> tools,
    List<GardenPhotoDto> photos,
    List<LatestCommentDto> barrage
) {
}
