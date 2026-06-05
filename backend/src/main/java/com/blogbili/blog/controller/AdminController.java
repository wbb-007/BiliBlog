package com.blogbili.blog.controller;

import com.blogbili.blog.model.AdminAlbumPhotoDto;
import com.blogbili.blog.model.AdminAlbumPhotoRequest;
import com.blogbili.blog.model.AdminAnnouncementDto;
import com.blogbili.blog.model.AdminAnnouncementRequest;
import com.blogbili.blog.model.AdminOverviewResponse;
import com.blogbili.blog.model.AdminPostUpdateRequest;
import com.blogbili.blog.model.AdminUserDto;
import com.blogbili.blog.model.AdminUserUpdateRequest;
import com.blogbili.blog.model.Live2dSettingsDto;
import com.blogbili.blog.model.Live2dSettingsRequest;
import com.blogbili.blog.model.PostSummary;
import com.blogbili.blog.model.ProfileSettingsDto;
import com.blogbili.blog.model.ProfileSettingsRequest;
import com.blogbili.blog.service.CurrentUser;
import com.blogbili.blog.service.AdminService;
import com.blogbili.blog.service.AuthService;
import com.blogbili.blog.service.BlogDataService;
import com.blogbili.blog.service.Live2dSettingsService;
import com.blogbili.blog.service.ProfileSettingsService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final AuthService authService;
    private final AdminService adminService;
    private final BlogDataService blogDataService;
    private final Live2dSettingsService live2dSettingsService;
    private final ProfileSettingsService profileSettingsService;

    public AdminController(
        AuthService authService,
        AdminService adminService,
        BlogDataService blogDataService,
        Live2dSettingsService live2dSettingsService,
        ProfileSettingsService profileSettingsService
    ) {
        this.authService = authService;
        this.adminService = adminService;
        this.blogDataService = blogDataService;
        this.live2dSettingsService = live2dSettingsService;
        this.profileSettingsService = profileSettingsService;
    }

    @GetMapping("/overview")
    public AdminOverviewResponse overview(HttpServletRequest request) {
        authService.requireAdmin(request);
        return adminService.overview(blogDataService.listAllPostSummaries());
    }

    @GetMapping("/users")
    public List<AdminUserDto> users(HttpServletRequest request) {
        authService.requireAdmin(request);
        return adminService.users();
    }

    @PutMapping("/users/{id}")
    public AdminUserDto updateUser(
        HttpServletRequest request,
        @PathVariable Long id,
        @Valid @RequestBody AdminUserUpdateRequest payload
    ) {
        CurrentUser currentUser = authService.requireAdmin(request);
        return adminService.updateUser(id, payload, currentUser.id());
    }

    @DeleteMapping("/users/{id}")
    public void deleteUser(HttpServletRequest request, @PathVariable Long id) {
        CurrentUser currentUser = authService.requireAdmin(request);
        adminService.deleteUser(id, currentUser.id());
    }

    @GetMapping("/posts")
    public List<PostSummary> posts(HttpServletRequest request) {
        authService.requireAdmin(request);
        return blogDataService.listAllPostSummaries();
    }

    @PutMapping("/posts/{id}")
    public PostSummary updatePost(
        HttpServletRequest request,
        @PathVariable Long id,
        @Valid @RequestBody AdminPostUpdateRequest payload
    ) {
        authService.requireAdmin(request);
        return blogDataService.updatePost(id, payload);
    }

    @DeleteMapping("/posts/{id}")
    public void deletePost(HttpServletRequest request, @PathVariable Long id) {
        authService.requireAdmin(request);
        adminService.deletePost(id);
    }

    @GetMapping("/announcements")
    public List<AdminAnnouncementDto> announcements(HttpServletRequest request) {
        authService.requireAdmin(request);
        return adminService.announcements();
    }

    @PostMapping("/announcements")
    public AdminAnnouncementDto createAnnouncement(
        HttpServletRequest request,
        @Valid @RequestBody AdminAnnouncementRequest payload
    ) {
        authService.requireAdmin(request);
        return adminService.createAnnouncement(payload);
    }

    @PutMapping("/announcements/{id}")
    public AdminAnnouncementDto updateAnnouncement(
        HttpServletRequest request,
        @PathVariable Long id,
        @Valid @RequestBody AdminAnnouncementRequest payload
    ) {
        authService.requireAdmin(request);
        return adminService.updateAnnouncement(id, payload);
    }

    @DeleteMapping("/announcements/{id}")
    public void deleteAnnouncement(HttpServletRequest request, @PathVariable Long id) {
        authService.requireAdmin(request);
        adminService.deleteAnnouncement(id);
    }

    @GetMapping("/album/photos")
    public List<AdminAlbumPhotoDto> albumPhotos(HttpServletRequest request) {
        authService.requireAdmin(request);
        return adminService.albumPhotos();
    }

    @PostMapping("/album/photos")
    public AdminAlbumPhotoDto createAlbumPhoto(
        HttpServletRequest request,
        @Valid @RequestBody AdminAlbumPhotoRequest payload
    ) {
        authService.requireAdmin(request);
        return adminService.createAlbumPhoto(payload);
    }

    @PutMapping("/album/photos/{id}")
    public AdminAlbumPhotoDto updateAlbumPhoto(
        HttpServletRequest request,
        @PathVariable Long id,
        @Valid @RequestBody AdminAlbumPhotoRequest payload
    ) {
        authService.requireAdmin(request);
        return adminService.updateAlbumPhoto(id, payload);
    }

    @DeleteMapping("/album/photos/{id}")
    public void deleteAlbumPhoto(HttpServletRequest request, @PathVariable Long id) {
        authService.requireAdmin(request);
        adminService.deleteAlbumPhoto(id);
    }

    @GetMapping("/live2d")
    public Live2dSettingsDto live2d(HttpServletRequest request) {
        authService.requireAdmin(request);
        return live2dSettingsService.getSettings();
    }

    @PutMapping("/live2d")
    public Live2dSettingsDto updateLive2d(
        HttpServletRequest request,
        @Valid @RequestBody Live2dSettingsRequest payload
    ) {
        authService.requireAdmin(request);
        return live2dSettingsService.updateSettings(payload);
    }

    @GetMapping("/profile")
    public ProfileSettingsDto profile(HttpServletRequest request) {
        authService.requireAdmin(request);
        return profileSettingsService.getSettings();
    }

    @PutMapping("/profile")
    public ProfileSettingsDto updateProfile(
        HttpServletRequest request,
        @RequestBody ProfileSettingsRequest payload
    ) {
        authService.requireAdmin(request);
        return profileSettingsService.updateSettings(payload);
    }
}
