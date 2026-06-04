package com.blogbili.blog.controller;

import com.blogbili.blog.model.AdminAnnouncementDto;
import com.blogbili.blog.model.AdminAnnouncementRequest;
import com.blogbili.blog.model.AdminOverviewResponse;
import com.blogbili.blog.model.AdminPostUpdateRequest;
import com.blogbili.blog.model.AdminUserDto;
import com.blogbili.blog.model.AdminUserUpdateRequest;
import com.blogbili.blog.model.PostSummary;
import com.blogbili.blog.service.CurrentUser;
import com.blogbili.blog.service.AdminService;
import com.blogbili.blog.service.AuthService;
import com.blogbili.blog.service.BlogDataService;
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

    public AdminController(
        AuthService authService,
        AdminService adminService,
        BlogDataService blogDataService
    ) {
        this.authService = authService;
        this.adminService = adminService;
        this.blogDataService = blogDataService;
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
}
