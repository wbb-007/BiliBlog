package com.blogbili.blog.service;

import com.blogbili.blog.entity.SiteSettingEntity;
import com.blogbili.blog.model.ProfileSettingsDto;
import com.blogbili.blog.model.ProfileSettingsRequest;
import com.blogbili.blog.repository.SiteSettingRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class ProfileSettingsService {

    private static final String SETTING_KEY = "profile";
    private static final String DEFAULT_AVATAR_LABEL = "AI";
    private static final String DEFAULT_NAME = "Kimi Chan";
    private static final String DEFAULT_HEADLINE = "插画师 / ACG 内容创作者";
    private static final String DEFAULT_BIO = "喜欢把博客写成带节奏感的内容首页，视觉上参考 bilibili，内容上保留个人博客的温度。";
    private static final String DEFAULT_BANNER_STYLE = "linear-gradient(135deg, #1f274f 0%, #6d3ecb 35%, #fb7299 100%)";

    private final SiteSettingRepository siteSettingRepository;
    private final ObjectMapper objectMapper;

    public ProfileSettingsService(SiteSettingRepository siteSettingRepository, ObjectMapper objectMapper) {
        this.siteSettingRepository = siteSettingRepository;
        this.objectMapper = objectMapper;
    }

    public ProfileSettingsDto getSettings() {
        return siteSettingRepository.findById(SETTING_KEY)
            .map(SiteSettingEntity::getValue)
            .map(this::readSettings)
            .orElseGet(this::defaultSettings);
    }

    @Transactional
    public ProfileSettingsDto updateSettings(ProfileSettingsRequest request) {
        ProfileSettingsDto settings = new ProfileSettingsDto(
            withDefault(request.name(), DEFAULT_NAME),
            withDefault(request.headline(), DEFAULT_HEADLINE),
            withDefault(request.bio(), DEFAULT_BIO),
            clean(request.avatarUrl()),
            cleanLabel(request.avatarLabel()),
            withDefault(request.bannerStyle(), DEFAULT_BANNER_STYLE),
            withDefault(request.followers(), "12.4w"),
            withDefault(request.likes(), "138.9w"),
            cleanTags(request.tags())
        );

        SiteSettingEntity entity = siteSettingRepository.findById(SETTING_KEY).orElseGet(SiteSettingEntity::new);
        entity.setKey(SETTING_KEY);
        try {
            entity.setValue(objectMapper.writeValueAsString(settings));
        } catch (JsonProcessingException error) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to save profile settings", error);
        }
        siteSettingRepository.save(entity);
        return settings;
    }

    private ProfileSettingsDto readSettings(String value) {
        try {
            ProfileSettingsDto stored = objectMapper.readValue(value, ProfileSettingsDto.class);
            return normalize(stored);
        } catch (JsonProcessingException error) {
            return defaultSettings();
        }
    }

    private ProfileSettingsDto defaultSettings() {
        return new ProfileSettingsDto(
            DEFAULT_NAME,
            DEFAULT_HEADLINE,
            DEFAULT_BIO,
            "",
            DEFAULT_AVATAR_LABEL,
            DEFAULT_BANNER_STYLE,
            "12.4w",
            "138.9w",
            List.of("个人博客", "视觉设计", "内容策展", "前端实验")
        );
    }

    private ProfileSettingsDto normalize(ProfileSettingsDto settings) {
        ProfileSettingsDto defaults = defaultSettings();
        return new ProfileSettingsDto(
            withDefault(settings.name(), defaults.name()),
            withDefault(settings.headline(), defaults.headline()),
            withDefault(settings.bio(), defaults.bio()),
            clean(settings.avatarUrl()),
            cleanLabel(settings.avatarLabel()),
            withDefault(settings.bannerStyle(), defaults.bannerStyle()),
            withDefault(settings.followers(), defaults.followers()),
            withDefault(settings.likes(), defaults.likes()),
            cleanTags(settings.tags()).isEmpty() ? defaults.tags() : cleanTags(settings.tags())
        );
    }

    private String clean(String value) {
        return value == null ? "" : value.trim();
    }

    private String withDefault(String value, String fallback) {
        String cleaned = clean(value);
        return cleaned.isBlank() ? fallback : cleaned;
    }

    private String cleanLabel(String value) {
        String label = clean(value);
        if (label.isBlank()) {
            return DEFAULT_AVATAR_LABEL;
        }
        return label.length() > 4 ? label.substring(0, 4) : label;
    }

    private List<String> cleanTags(List<String> tags) {
        if (tags == null) {
            return List.of();
        }
        return tags.stream()
            .map(this::clean)
            .filter(tag -> !tag.isBlank())
            .distinct()
            .limit(8)
            .toList();
    }
}
