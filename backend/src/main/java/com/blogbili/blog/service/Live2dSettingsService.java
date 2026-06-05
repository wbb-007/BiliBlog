package com.blogbili.blog.service;

import com.blogbili.blog.entity.SiteSettingEntity;
import com.blogbili.blog.model.Live2dPresetDto;
import com.blogbili.blog.model.Live2dSettingsDto;
import com.blogbili.blog.model.Live2dSettingsRequest;
import com.blogbili.blog.repository.SiteSettingRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class Live2dSettingsService {

    private static final String SETTING_KEY = "live2d";

    private static final List<Live2dPresetDto> PRESETS = List.of(
        new Live2dPresetDto("Haru", "https://unpkg.com/live2d-widget-model-haru@1.0.5/assets/haru01.model.json"),
        new Live2dPresetDto("Koharu", "https://unpkg.com/live2d-widget-model-haru@1.0.5/assets/haru02.model.json"),
        new Live2dPresetDto("Shizuku", "https://unpkg.com/live2d-widget-model-shizuku@1.0.5/assets/shizuku.model.json"),
        new Live2dPresetDto("Wanko", "https://unpkg.com/live2d-widget-model-wanko@1.0.5/assets/wanko.model.json"),
        new Live2dPresetDto("Z16", "https://unpkg.com/live2d-widget-model-z16@1.0.5/assets/z16.model.json")
    );

    private final SiteSettingRepository siteSettingRepository;
    private final ObjectMapper objectMapper;

    public Live2dSettingsService(SiteSettingRepository siteSettingRepository, ObjectMapper objectMapper) {
        this.siteSettingRepository = siteSettingRepository;
        this.objectMapper = objectMapper;
    }

    public Live2dSettingsDto getSettings() {
        return siteSettingRepository.findById(SETTING_KEY)
            .map(SiteSettingEntity::getValue)
            .map(this::readSettings)
            .orElseGet(this::defaultSettings);
    }

    @Transactional
    public Live2dSettingsDto updateSettings(Live2dSettingsRequest request) {
        Live2dSettingsDto settings = new Live2dSettingsDto(
            request.enabled(),
            cleanModelName(request.modelName(), request.modelUrl()),
            request.modelUrl().trim(),
            normalizePosition(request.position()),
            request.width(),
            request.height(),
            request.hOffset(),
            request.vOffset(),
            request.scale(),
            PRESETS
        );

        SiteSettingEntity entity = siteSettingRepository.findById(SETTING_KEY).orElseGet(SiteSettingEntity::new);
        entity.setKey(SETTING_KEY);
        try {
            entity.setValue(objectMapper.writeValueAsString(settings));
        } catch (JsonProcessingException error) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to save Live2D settings", error);
        }
        siteSettingRepository.save(entity);
        return settings;
    }

    private Live2dSettingsDto readSettings(String value) {
        try {
            Live2dSettingsDto stored = objectMapper.readValue(value, Live2dSettingsDto.class);
            return new Live2dSettingsDto(
                stored.enabled(),
                cleanModelName(stored.modelName(), stored.modelUrl()),
                stored.modelUrl(),
                normalizePosition(stored.position()),
                stored.width() <= 0 ? 280 : stored.width(),
                stored.height() <= 0 ? 360 : stored.height(),
                stored.hOffset(),
                stored.vOffset(),
                stored.scale() <= 0 ? 1.0 : stored.scale(),
                PRESETS
            );
        } catch (JsonProcessingException error) {
            return defaultSettings();
        }
    }

    private Live2dSettingsDto defaultSettings() {
        Live2dPresetDto defaultPreset = PRESETS.getFirst();
        return new Live2dSettingsDto(
            true,
            defaultPreset.name(),
            defaultPreset.modelUrl(),
            "right",
            280,
            360,
            16,
            0,
            1.0,
            PRESETS
        );
    }

    private String cleanModelName(String modelName, String modelUrl) {
        if (modelName != null && !modelName.isBlank()) {
            return modelName.trim();
        }
        return PRESETS.stream()
            .filter(item -> item.modelUrl().equals(modelUrl))
            .map(Live2dPresetDto::name)
            .findFirst()
            .orElse("Custom");
    }

    private String normalizePosition(String position) {
        return "left".equalsIgnoreCase(position) ? "left" : "right";
    }
}
