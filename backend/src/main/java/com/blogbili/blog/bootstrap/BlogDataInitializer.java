package com.blogbili.blog.bootstrap;

import com.blogbili.blog.entity.AnnouncementEntity;
import com.blogbili.blog.entity.BlogPostEntity;
import com.blogbili.blog.entity.UserEntity;
import com.blogbili.blog.repository.AnnouncementRepository;
import com.blogbili.blog.repository.BlogPostRepository;
import com.blogbili.blog.repository.UserRepository;
import com.blogbili.blog.service.PasswordCodec;
import java.util.List;
import java.util.Locale;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class BlogDataInitializer implements CommandLineRunner {

    private final BlogPostRepository blogPostRepository;
    private final AnnouncementRepository announcementRepository;
    private final UserRepository userRepository;
    private final PasswordCodec passwordCodec;

    @Value("${blog.admin.email}")
    private String adminEmail;

    @Value("${blog.admin.nickname}")
    private String adminNickname;

    @Value("${blog.admin.initial-password:Admin@123456}")
    private String adminInitialPassword;

    public BlogDataInitializer(
        BlogPostRepository blogPostRepository,
        AnnouncementRepository announcementRepository,
        UserRepository userRepository,
        PasswordCodec passwordCodec
    ) {
        this.blogPostRepository = blogPostRepository;
        this.announcementRepository = announcementRepository;
        this.userRepository = userRepository;
        this.passwordCodec = passwordCodec;
    }

    @Override
    public void run(String... args) {
        UserEntity adminUser = ensureAdminUser();
        ensureLegacyPostsOwnedByAdmin(adminUser);
        removeDemoSeedData();
    }

    private UserEntity ensureAdminUser() {
        String normalizedEmail = adminEmail.trim().toLowerCase(Locale.ROOT);
        UserEntity adminUser = userRepository.findByEmail(normalizedEmail).orElseGet(UserEntity::new);

        adminUser.setEmail(normalizedEmail);
        adminUser.setNickname(
            adminUser.getNickname() == null || adminUser.getNickname().isBlank()
                ? adminNickname
                : adminUser.getNickname()
        );
        if (adminUser.getPasswordHash() == null || adminUser.getPasswordHash().isBlank()) {
            adminUser.setPasswordHash(passwordCodec.encode(adminInitialPassword));
        }
        adminUser.setRole(UserEntity.Role.ADMIN);
        adminUser.setStatus(UserEntity.Status.ACTIVE);
        return userRepository.save(adminUser);
    }

    private void ensureLegacyPostsOwnedByAdmin(UserEntity adminUser) {
        List<BlogPostEntity> posts = blogPostRepository.findAll().stream()
            .filter(post -> post.getAuthorUserId() == null || !post.hasStoredAuthorType())
            .peek(post -> {
                if (post.getAuthorUserId() == null) {
                    post.setAuthorUserId(adminUser.getId());
                }
                if (!post.hasStoredAuthorType()) {
                    post.setAuthorType(BlogPostEntity.AuthorType.ADMIN);
                }
            })
            .toList();

        if (!posts.isEmpty()) {
            blogPostRepository.saveAll(posts);
        }
    }

    private void removeDemoSeedData() {
        List<String> demoPostTitles = List.of(
            "把个人博客做成 B 站风内容首页，我是怎么拆页面节奏的",
            "从动画区灵感到博客专题页：一套高留存封面应该怎么搭",
            "个人空间页怎么做得不像简历，而像一个有生命力的创作橱窗",
            "把发文后台先做顺，再考虑富文本：这是博客项目最省力的顺序",
            "为什么我保留了 bilibili 的粉蓝气质，但没有照搬它的所有组件",
            "周末复盘：这一版博客骨架已经能支撑下一步接数据库和登录了"
        );

        List<BlogPostEntity> allPosts = blogPostRepository.findAll();
        boolean exactDemoPosts =
            allPosts.size() == demoPostTitles.size() &&
            allPosts.stream().allMatch(post -> demoPostTitles.contains(post.getTitle()));
        if (exactDemoPosts) {
            blogPostRepository.deleteAll(allPosts);
        }

        List<String> demoAnnouncements = List.of("欢迎来到 BiliBlog", "管理员提示", "开发模式说明");
        List<AnnouncementEntity> allAnnouncements = announcementRepository.findAll();
        boolean exactDemoAnnouncements =
            allAnnouncements.size() == demoAnnouncements.size() &&
            allAnnouncements.stream().allMatch(item -> demoAnnouncements.contains(item.getTitle()));
        if (exactDemoAnnouncements) {
            announcementRepository.deleteAll(allAnnouncements);
        }
    }
}
