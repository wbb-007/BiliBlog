package com.blogbili.blog.model;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record Live2dSettingsRequest(
    boolean enabled,
    String modelName,
    @NotBlank(message = "modelUrl is required")
    String modelUrl,
    String position,
    @Min(120)
    @Max(600)
    int width,
    @Min(120)
    @Max(800)
    int height,
    @Min(-200)
    @Max(200)
    int hOffset,
    @Min(-200)
    @Max(200)
    int vOffset,
    @Min(1)
    @Max(3)
    double scale
) {
}
