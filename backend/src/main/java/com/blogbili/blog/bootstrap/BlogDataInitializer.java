package com.blogbili.blog.bootstrap;

import com.blogbili.blog.entity.AnnouncementEntity;
import com.blogbili.blog.entity.BlogCommentEntity;
import com.blogbili.blog.entity.BlogPostEntity;
import com.blogbili.blog.entity.UserEntity;
import com.blogbili.blog.repository.AnnouncementRepository;
import com.blogbili.blog.repository.BlogCommentRepository;
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
    private final BlogCommentRepository blogCommentRepository;
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
        BlogCommentRepository blogCommentRepository,
        AnnouncementRepository announcementRepository,
        UserRepository userRepository,
        PasswordCodec passwordCodec
    ) {
        this.blogPostRepository = blogPostRepository;
        this.blogCommentRepository = blogCommentRepository;
        this.announcementRepository = announcementRepository;
        this.userRepository = userRepository;
        this.passwordCodec = passwordCodec;
    }

    @Override
    public void run(String... args) {
        UserEntity adminUser = ensureAdminUser();
        ensureLegacyPostsOwnedByAdmin(adminUser);
        removeDemoSeedData();
        ensureAiTutorialPosts(adminUser);
        ensureAiTutorialComments();
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

    private void ensureAiTutorialPosts(UserEntity adminUser) {
        List<BlogPostEntity> posts = List.of(
            aiPost(
                adminUser,
                "AI 教程 01：从提示词开始搭一个个人知识助手",
                "用最小成本把资料整理、角色设定、提示词模板和输出检查串起来，先做一个能帮你读文章、写摘要、列行动清单的小助手。",
                "提示词入门",
                "linear-gradient(135deg, #fb7299 0%, #ffb7cc 38%, #5ac8fa 100%)",
                List.of("AI 教程", "提示词", "知识助手"),
                """
                ## 先定义助手边界
                一个好用的 AI 助手不需要一开始就很复杂。先写清楚三件事：它服务谁、接收什么材料、输出什么结果。

                - 角色：个人学习助理
                - 输入：文章、笔记、链接摘要或课堂记录
                - 输出：三段摘要、五个要点、下一步行动清单

                ## 提示词模板
                > 你是我的学习助理。请先判断材料主题，再用中文输出摘要、重点、可执行行动，不确定的地方单独列为待确认。

                ## 迭代方法
                第一次不要追求完美，先让它稳定输出固定格式。之后再逐步增加“引用原文位置”“标出风险点”“给我追问问题”这些能力。
                """
            ),
            aiPost(
                adminUser,
                "AI 教程 02：RAG 是什么，博客也能做资料问答",
                "用博客文章作为知识库，理解切分、向量检索、重排和回答生成的完整流程。",
                "RAG 实战",
                "linear-gradient(135deg, #2fc89f 0%, #78e4be 45%, #5ac8fa 100%)",
                List.of("AI 教程", "RAG", "知识库"),
                """
                ## RAG 的核心流程
                RAG 可以理解为“先找资料，再让模型回答”。它不是让模型背下你的全部内容，而是在回答前把相关片段找出来。

                - 文档切分：按标题、段落或语义切块
                - 向量化：把文本转成可检索的向量
                - 检索：找出和问题最相关的片段
                - 生成：把片段和问题一起交给模型回答

                ## 博客场景怎么用
                个人博客很适合做 RAG，因为文章结构天然清楚。可以先把每篇文章的标题、标签、摘要和正文切块，用户提问时优先返回自己的内容。

                ## 注意事项
                不要只看答案是否流畅，还要检查引用片段是否真的支持结论。RAG 的质量往往卡在资料清洗和检索策略上。
                """
            ),
            aiPost(
                adminUser,
                "AI 教程 03：用 AI 做前端页面评审清单",
                "把视觉检查、可访问性、响应式和交互状态整理成固定提示词，让 AI 帮你做发布前走查。",
                "前端 AI 工作流",
                "linear-gradient(135deg, #1f274f 0%, #6d3ecb 38%, #fb7299 100%)",
                List.of("AI 教程", "前端", "代码评审"),
                """
                ## 为什么要固定清单
                AI 很擅长发现遗漏，但前提是你给它稳定的检查维度。前端页面可以从布局、文字、状态、移动端和可访问性五个角度走查。

                ## 推荐检查项
                - 页面首屏是否直接呈现核心功能
                - 按钮、输入框、空状态和加载状态是否完整
                - 移动端是否出现文字溢出或内容重叠
                - 图片是否有明确尺寸和替代文本
                - 颜色是否过度依赖单一色系

                ## 使用方式
                每次提交前把页面截图、相关组件代码和目标用户告诉 AI，让它只输出问题、风险和建议修改点。这样结果会更像真正的评审。
                """
            )
        );

        posts.stream()
            .filter(post -> !blogPostRepository.existsByTitle(post.getTitle()))
            .forEach(blogPostRepository::save);
    }

    private void ensureAiTutorialComments() {
        if (blogCommentRepository.count() > 0) {
            return;
        }

        List.of(
            comment("AI 教程 01：从提示词开始搭一个个人知识助手", "Prompt小栈", "这个模板很适合拿去做自己的读书摘要流，先固定输出格式确实省心。"),
            comment("AI 教程 02：RAG 是什么，博客也能做资料问答", "向量检索练习生", "把博客内容做 RAG 这个思路不错，后面想看切分策略的具体代码。"),
            comment("AI 教程 03：用 AI 做前端页面评审清单", "前端自检员", "发布前让 AI 按清单挑问题很实用，移动端溢出检查救命。")
        ).stream()
            .flatMap(java.util.Optional::stream)
            .forEach(blogCommentRepository::save);
    }

    private java.util.Optional<BlogCommentEntity> comment(String postTitle, String nickname, String content) {
        return blogPostRepository.findByTitle(postTitle).map(post -> {
            BlogCommentEntity comment = new BlogCommentEntity();
            comment.setPost(post);
            comment.setUserNickname(nickname);
            comment.setUserInitial(nickname.substring(0, 1));
            comment.setContent(content);
            return comment;
        });
    }

    private BlogPostEntity aiPost(
        UserEntity adminUser,
        String title,
        String excerpt,
        String coverLabel,
        String coverStyle,
        List<String> tags,
        String content
    ) {
        BlogPostEntity post = new BlogPostEntity();
        post.setTitle(title);
        post.setExcerpt(excerpt);
        post.setCategorySlug("tech");
        post.setCategoryName("AI 教程");
        post.setPublishedAt(java.time.LocalDate.now());
        post.setViews("2.6k");
        post.setComments("0");
        post.setLikes("680");
        post.setFavorites("241");
        post.setShares("32");
        post.setReadTime("6 分钟");
        post.setCoverLabel(coverLabel);
        post.setCoverStyle(coverStyle);
        post.setIntro(excerpt);
        post.setAuthorName(adminUser.getNickname());
        post.setAuthorTitle("AI 教程作者 / 博主");
        post.setAuthorAvatarLabel(adminUser.getNickname().substring(0, 1));
        post.setAuthorFollowers("12.4w");
        post.setAuthorArticles(String.valueOf(blogPostRepository.countByAuthorUserId(adminUser.getId()) + 1));
        post.setAuthorUserId(adminUser.getId());
        post.setAuthorType(BlogPostEntity.AuthorType.ADMIN);
        post.setTagsText(String.join("||", tags));
        post.setContentText(content.trim());
        return post;
    }
}
