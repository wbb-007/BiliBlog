package com.blogbili.blog.controller;

import com.blogbili.blog.model.Live2dSettingsDto;
import com.blogbili.blog.service.Live2dSettingsService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/site")
public class SiteController {

    private final Live2dSettingsService live2dSettingsService;

    public SiteController(Live2dSettingsService live2dSettingsService) {
        this.live2dSettingsService = live2dSettingsService;
    }

    @GetMapping("/live2d")
    public Live2dSettingsDto live2d() {
        return live2dSettingsService.getSettings();
    }
}
