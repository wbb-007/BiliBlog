package com.blogbili.blog.service;

import com.blogbili.blog.entity.AnnouncementEntity;
import com.blogbili.blog.entity.BlogPostEntity;
import com.blogbili.blog.entity.UserEntity;
import com.blogbili.blog.model.AdminAnnouncementDto;
import com.blogbili.blog.model.AdminAnnouncementRequest;
import com.blogbili.blog.model.AdminOverviewResponse;
import com.blogbili.blog.model.AdminPostUpdateRequest;
import com.blogbili.blog.model.AdminUserDto;
import com.blogbili.blog.model.AdminUserUpdateRequest;
import com.blogbili.blog.model.Metric;
import com.blogbili.blog.model.PostSummary;
import com.blogbili.blog.model.UserSessionDto;
import com.blogbili.blog.repository.AuthSessionRepository;
import com.blogbili.blog.repository.AnnouncementRepository;
import com.blogbili.blog.repository.BlogCommentRepository;
import com.blogbili.blog.repository.BlogPostRepository;
import com.blogbili.blog.repository.UserRepository;
import java.time.format.DateTimeFormatter;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AdminService {

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final BlogPostRepository blogPostRepository;
    private final UserRepository userRepository;
    private final BlogCommentRepository blogCommentRepository;
    private final AnnouncementRepository announcementRepository;
    private final AuthSessionRepository authSessionRepository;

    public AdminService(
        BlogPostRepository blogPostRepository,
        UserRepository userRepository,
        BlogCommentRepository blogCommentRepository,
        AnnouncementRepository announcementRepository,
        AuthSessionRepository authSessionRepository
    ) {
        this.blogPostRepository = blogPostRepository;
        this.userRepository = userRepository;
        this.blogCommentRepository = blogCommentRepository;
        this.announcementRepository = announcementRepository;
        this.authSessionRepository = authSessionRepository;
    }

    public AdminOverviewResponse overview(List<PostSummary> posts) {
        return new AdminOverviewResponse(
            List.of(
                new Metric("文章总数", String.valueOf(blogPostRepository.count())),
                new Metric("账号总数", String.valueOf(userRepository.count())),
                new Metric("评论总数", String.valueOf(blogCommentRepository.count())),
                new Metric("公告推送", String.valueOf(announcementRepository.count()))
            ),
            posts.stream().limit(5).toList(),
            userRepository.findAll().stream()
                .sorted((left, right) -> right.getCreatedAt().compareTo(left.getCreatedAt()))
                .limit(5)
                .map(this::toUserSession)
                .toList(),
            announcementRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(this::toAnnouncementDto)
                .toList()
        );
    }

    public List<AdminUserDto> users() {
        return userRepository.findAll().stream()
            .sorted((left, right) -> right.getCreatedAt().compareTo(left.getCreatedAt()))
            .map(this::toAdminUser)
            .toList();
    }

    @Transactional
    public AdminUserDto updateUser(Long id, AdminUserUpdateRequest request, Long operatorUserId) {
        UserEntity user = userRepository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "用户不存在"));

        UserEntity.Role nextRole = parseRole(request.role());
        UserEntity.Status nextStatus = parseStatus(request.status());

        if (user.getId().equals(operatorUserId)) {
            if (nextRole != user.getRole()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "不能修改当前登录管理员自己的角色");
            }
            if (nextStatus != user.getStatus()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "不能停用当前登录管理员自己");
            }
        }

        boolean removingActiveAdmin =
            user.getRole() == UserEntity.Role.ADMIN &&
            user.getStatus() == UserEntity.Status.ACTIVE &&
            (nextRole != UserEntity.Role.ADMIN || nextStatus != UserEntity.Status.ACTIVE);

        if (removingActiveAdmin && activeAdminCount() <= 1) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "站点至少需要保留一个启用中的管理员");
        }

        user.setRole(nextRole);
        user.setStatus(nextStatus);
        UserEntity saved = userRepository.save(user);

        if (nextStatus == UserEntity.Status.DISABLED) {
            authSessionRepository.deleteByUser_Id(saved.getId());
        }

        return toAdminUser(saved);
    }

    @Transactional
    public BlogPostEntity updatePost(Long id, AdminPostUpdateRequest request, String categoryName, String coverStyle) {
        BlogPostEntity post = blogPostRepository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "文章不存在"));

        post.setTitle(request.title().trim());
        post.setExcerpt(request.excerpt().trim());
        post.setIntro(request.excerpt().trim());
        post.setCategorySlug(request.category());
        post.setCategoryName(categoryName);
        post.setTagsText(String.join("||", request.tags() == null ? List.of() : request.tags()));
        post.setContentText(request.content().trim());
        post.setCoverStyle(coverStyle);
        post.setCoverLabel("Admin Updated");
        return blogPostRepository.save(post);
    }

    @Transactional
    public void deletePost(Long id) {
        if (!blogPostRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "文章不存在");
        }
        blogCommentRepository.deleteByPost_Id(id);
        blogPostRepository.deleteById(id);
    }

    @Transactional
    public void deleteUser(Long id, Long operatorUserId) {
        UserEntity user = userRepository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "用户不存在"));

        if (user.getId().equals(operatorUserId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "不能删除当前登录管理员自己");
        }

        if (user.getRole() == UserEntity.Role.ADMIN && user.getStatus() == UserEntity.Status.ACTIVE && activeAdminCount() <= 1) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "站点至少需要保留一个启用中的管理员");
        }

        List<BlogPostEntity> authoredPosts = blogPostRepository.findAllByAuthorUserId(id);
        for (BlogPostEntity post : authoredPosts) {
            blogCommentRepository.deleteByPost_Id(post.getId());
        }

        authSessionRepository.deleteByUser_Id(id);
        blogCommentRepository.deleteByUser_Id(id);
        blogPostRepository.deleteAll(authoredPosts);
        userRepository.delete(user);
    }

    public List<AdminAnnouncementDto> announcements() {
        return announcementRepository.findAllByOrderByCreatedAtDesc().stream()
            .map(this::toAnnouncementDto)
            .toList();
    }

    @Transactional
    public AdminAnnouncementDto createAnnouncement(AdminAnnouncementRequest request) {
        AnnouncementEntity announcement = new AnnouncementEntity();
        announcement.setTitle(request.title().trim());
        announcement.setContent(request.content().trim());
        announcement.setActive(request.active());
        return toAnnouncementDto(announcementRepository.save(announcement));
    }

    @Transactional
    public AdminAnnouncementDto updateAnnouncement(Long id, AdminAnnouncementRequest request) {
        AnnouncementEntity announcement = announcementRepository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "公告不存在"));
        announcement.setTitle(request.title().trim());
        announcement.setContent(request.content().trim());
        announcement.setActive(request.active());
        return toAnnouncementDto(announcementRepository.save(announcement));
    }

    @Transactional
    public void deleteAnnouncement(Long id) {
        if (!announcementRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "公告不存在");
        }
        announcementRepository.deleteById(id);
    }

    private UserSessionDto toUserSession(UserEntity user) {
        return new UserSessionDto(user.getId(), user.getEmail(), user.getNickname(), user.getRole().name());
    }

    private AdminUserDto toAdminUser(UserEntity user) {
        return new AdminUserDto(
            user.getId(),
            user.getEmail(),
            user.getNickname(),
            user.getRole().name(),
            user.getStatus().name(),
            user.getCreatedAt().format(DATE_TIME_FORMATTER),
            user.getLastLoginAt() == null ? null : user.getLastLoginAt().format(DATE_TIME_FORMATTER)
        );
    }

    private UserEntity.Role parseRole(String role) {
        try {
            return UserEntity.Role.valueOf(role.trim().toUpperCase());
        } catch (Exception ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "无效的用户角色");
        }
    }

    private UserEntity.Status parseStatus(String status) {
        try {
            return UserEntity.Status.valueOf(status.trim().toUpperCase());
        } catch (Exception ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "无效的用户状态");
        }
    }

    private long activeAdminCount() {
        return userRepository.findAll().stream()
            .filter(user -> user.getRole() == UserEntity.Role.ADMIN)
            .filter(user -> user.getStatus() == UserEntity.Status.ACTIVE)
            .count();
    }

    private AdminAnnouncementDto toAnnouncementDto(AnnouncementEntity entity) {
        return new AdminAnnouncementDto(
            entity.getId(),
            entity.getTitle(),
            entity.getContent(),
            entity.isActive(),
            entity.getCreatedAt().format(DATE_TIME_FORMATTER)
        );
    }
}
