package com.blogbili.blog.model;

import java.util.List;

public record Live2dSettingsDto(
    boolean enabled,
    String modelName,
    String modelUrl,
    String position,
    int width,
    int height,
    int hOffset,
    int vOffset,
    double scale,
    List<Live2dPresetDto> presets
) {
}
