package com.blogbili.blog.service;

import com.blogbili.blog.entity.AnnouncementEntity;
import com.blogbili.blog.entity.BlogCommentEntity;
import com.blogbili.blog.entity.BlogPostEntity;
import com.blogbili.blog.model.AdminPostUpdateRequest;
import com.blogbili.blog.model.Announcement;
import com.blogbili.blog.model.AuthorProfile;
import com.blogbili.blog.model.CategoryDto;
import com.blogbili.blog.model.CommentCreateRequest;
import com.blogbili.blog.model.CommentDto;
import com.blogbili.blog.model.CommunityResponse;
import com.blogbili.blog.model.ContentBlock;
import com.blogbili.blog.model.CreatorProfile;
import com.blogbili.blog.model.DetailedPost;
import com.blogbili.blog.model.HomeResponse;
import com.blogbili.blog.model.Metric;
import com.blogbili.blog.model.PostDetailResponse;
import com.blogbili.blog.model.PostStats;
import com.blogbili.blog.model.PostSummary;
import com.blogbili.blog.model.ProfileIdentity;
import com.blogbili.blog.model.ProfileResponse;
import com.blogbili.blog.model.PublishPostRequest;
import com.blogbili.blog.model.PublishPostResponse;
import com.blogbili.blog.model.TimelineItem;
import com.blogbili.blog.repository.AnnouncementRepository;
import com.blogbili.blog.repository.BlogCommentRepository;
import com.blogbili.blog.repository.BlogPostRepository;
import com.blogbili.blog.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class BlogDataService {

    private static final String DEFAULT_TAG_DELIMITER = "\\|\\|";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_DATE;
    private static final DateTimeFormatter COMMENT_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final BlogPostRepository blogPostRepository;
    private final BlogCommentRepository blogCommentRepository;
    private final AnnouncementRepository announcementRepository;
    private final UserRepository userRepository;
    private final AuthService authService;
    private final AdminService adminService;

    private final List<CategoryDto> categories = List.of(
        new CategoryDto("anime", "动画杂谈"),
        new CategoryDto("games", "游戏映像"),
        new CategoryDto("tech", "技术实验室"),
        new CategoryDto("life", "生活记录")
    );

    private final CreatorProfile creatorProfile = new CreatorProfile(
        "Kimi Chan",
        "插画师 / ACG 内容创作者",
        "喜欢把博客写成带节奏感的内容首页，视觉上参考 bilibili，内容上保留个人博客的温度。",
        "KC",
        "linear-gradient(135deg, #1f274f 0%, #6d3ecb 35%, #fb7299 100%)",
        "12.4w",
        "138.9w",
        "48",
        List.of("个人博客", "视觉设计", "内容策展", "前端实验")
    );

    public BlogDataService(
        BlogPostRepository blogPostRepository,
        BlogCommentRepository blogCommentRepository,
        AnnouncementRepository announcementRepository,
        UserRepository userRepository,
        AuthService authService,
        AdminService adminService
    ) {
        this.blogPostRepository = blogPostRepository;
        this.blogCommentRepository = blogCommentRepository;
        this.announcementRepository = announcementRepository;
        this.userRepository = userRepository;
        this.authService = authService;
        this.adminService = adminService;
    }

    public HomeResponse getHome() {
        List<PostSummary> orderedPosts = listAdminPostSummaries();
        PostSummary featured = orderedPosts.isEmpty() ? null : orderedPosts.getFirst();
        List<PostSummary> spotlight = orderedPosts.stream().skip(featured == null ? 0 : 1).limit(2).toList();
        List<PostSummary> feed = orderedPosts.stream().skip(featured == null ? 0 : 3).limit(6).toList();

        return new HomeResponse(
            categories,
            featured,
            spotlight,
            feed,
            creatorProfile,
            announcementRepository.findAllByOrderByCreatedAtDesc().stream()
                .filter(AnnouncementEntity::isActive)
                .limit(3)
                .map(item -> new Announcement(item.getTitle(), item.getContent()))
                .toList(),
            List.of(
                new Metric("日均访问", "8.4k"),
                new Metric("人均停留", "6m 28s"),
                new Metric("站点文章", String.valueOf(blogPostRepository.countByAuthorType(BlogPostEntity.AuthorType.ADMIN))),
                new Metric("评论总数", String.valueOf(blogCommentRepository.count()))
            )
        );
    }

    public CommunityResponse getCommunity() {
        return new CommunityResponse(
            List.of(),
            List.of(
                new Metric("当前状态", "已关闭投稿"),
                new Metric("主站文章", String.valueOf(blogPostRepository.countByAuthorType(BlogPostEntity.AuthorType.ADMIN))),
                new Metric("评论互动", String.valueOf(blogCommentRepository.count()))
            )
        );
    }

    public List<CategoryDto> getCategories() {
        return categories;
    }

    public PostDetailResponse getPost(Long id) {
        BlogPostEntity entity = blogPostRepository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Post not found"));

        List<PostSummary> related = listSummariesByAuthorType(entity.getAuthorType()).stream()
            .filter(item -> !item.id().equals(id))
            .limit(3)
            .toList();

        return new PostDetailResponse(
            toDetailedPost(entity),
            getPostComments(id),
            related
        );
    }

    public List<CommentDto> getPostComments(Long postId) {
        return blogCommentRepository.findAllByPost_IdOrderByCreatedAtDesc(postId).stream()
            .map(comment -> new CommentDto(
                comment.getId(),
                comment.getUserNickname(),
                comment.getUserInitial(),
                comment.getCreatedAt().format(COMMENT_TIME_FORMATTER),
                comment.getContent()
            ))
            .toList();
    }

    public CommentDto addComment(Long postId, CommentCreateRequest request, HttpServletRequest httpRequest) {
        BlogPostEntity post = blogPostRepository.findById(postId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "文章不存在"));
        String nickname = request.nickname().trim();

        BlogCommentEntity comment = new BlogCommentEntity();
        comment.setPost(post);
        comment.setUser(null);
        comment.setUserNickname(nickname);
        comment.setUserInitial(nickname.substring(0, 1));
        comment.setContent(request.content().trim());

        BlogCommentEntity saved = blogCommentRepository.save(comment);

        return new CommentDto(
            saved.getId(),
            saved.getUserNickname(),
            saved.getUserInitial(),
            saved.getCreatedAt().format(COMMENT_TIME_FORMATTER),
            saved.getContent()
        );
    }

    public ProfileResponse getProfile() {
        List<PostSummary> summaries = listAdminPostSummaries();
        return new ProfileResponse(
            new ProfileIdentity(
                creatorProfile.name(),
                creatorProfile.title(),
                creatorProfile.bio(),
                creatorProfile.avatarLabel(),
                creatorProfile.bannerStyle(),
                List.of(
                    new Metric("粉丝", "124k"),
                    new Metric("关注", "342"),
                    new Metric("获赞", "1.38m"),
                    new Metric("文章", String.valueOf(blogPostRepository.countByAuthorType(BlogPostEntity.AuthorType.ADMIN)))
                ),
                creatorProfile.tags(),
                "2026-05-21"
            ),
            summaries.stream().limit(3).toList(),
            summaries.stream().skip(1).limit(4).toList(),
            List.of(
                new TimelineItem("2026 · 互动体验收口", "前台聚焦阅读与评论，博客管理统一收进独立后台。"),
                new TimelineItem("2026 · 数据持久化", "博客文章已切换到数据库存储，发文结果可以跨重启保留。"),
                new TimelineItem("2024 · 个人品牌确立", "明确“轻盈、活泼、社区感”作为博客主视觉方向。")
            ),
            List.of(
                new Metric("动画观察", "16"),
                new Metric("设计拆解", "12"),
                new Metric("技术复盘", "9")
            ),
            List.of(
                new Metric("月浏览增长", "+28%"),
                new Metric("收藏增长", "+17%"),
                new Metric("评论活跃", "+" + blogCommentRepository.count())
            )
        );
    }

    public PublishPostResponse publish(PublishPostRequest request, HttpServletRequest httpRequest) {
        CurrentUser currentUser = authService.requireAdmin(httpRequest);
        List<String> tags = request.tags() == null || request.tags().isEmpty()
            ? List.of("新发布", "博客日常")
            : request.tags().stream().map(String::trim).filter(tag -> !tag.isBlank()).toList();

        BlogPostEntity post = new BlogPostEntity();
        post.setTitle(request.title().trim());
        post.setExcerpt(request.summary().trim());
        post.setCategorySlug(request.category());
        post.setCategoryName(categoryName(request.category()));
        post.setPublishedAt(LocalDate.now());
        post.setViews("1.1k");
        post.setComments("0");
        post.setLikes("342");
        post.setFavorites("118");
        post.setShares("14");
        post.setReadTime("5 分钟");
        post.setCoverLabel("New Story Drop");
        post.setCoverStyle(coverStyle(request.coverTone()));
        post.setIntro(request.summary().trim());
        post.setAuthorName(currentUser.nickname());
        post.setAuthorTitle("站点管理员 / 博主");
        post.setAuthorAvatarLabel(currentUser.nickname().substring(0, 1));
        post.setAuthorFollowers("12.4w");
        post.setAuthorArticles(String.valueOf(blogPostRepository.countByAuthorUserId(currentUser.id()) + 1));
        post.setAuthorUserId(currentUser.id());
        post.setAuthorType(BlogPostEntity.AuthorType.ADMIN);
        post.setTagsText(String.join("||", tags));
        post.setContentText(request.content().trim());

        BlogPostEntity saved = blogPostRepository.save(post);
        return new PublishPostResponse(
            saved.getId(),
            "主站文章发布成功，已经为你跳转到详情页。"
        );
    }

    public List<PostSummary> listAllPostSummaries() {
        return blogPostRepository.findAllByOrderByPublishedAtDescIdDesc().stream()
            .map(this::toSummary)
            .toList();
    }

    public List<PostSummary> listAdminPostSummaries() {
        return listSummariesByAuthorType(BlogPostEntity.AuthorType.ADMIN);
    }

    public List<PostSummary> listUserPostSummaries() {
        return listSummariesByAuthorType(BlogPostEntity.AuthorType.USER);
    }

    public PostSummary updatePost(Long id, AdminPostUpdateRequest request) {
        String categoryName = categoryName(request.category());
        String coverStyle = coverStyle(request.coverTone());
        return toSummary(adminService.updatePost(id, request, categoryName, coverStyle));
    }

    private PostSummary toSummary(BlogPostEntity entity) {
        String commentCount = commentCountText(entity.getId());
        return new PostSummary(
            entity.getId(),
            entity.getTitle(),
            entity.getExcerpt(),
            entity.getCategoryName(),
            boardLabel(entity.getAuthorType()),
            entity.getPublishedAt().format(DATE_FORMATTER),
            entity.getViews(),
            commentCount,
            entity.getLikes(),
            entity.getReadTime(),
            entity.getCoverLabel(),
            entity.getCoverStyle(),
            parseTags(entity.getTagsText()),
            entity.getAuthorName(),
            entity.getAuthorAvatarLabel()
        );
    }

    private DetailedPost toDetailedPost(BlogPostEntity entity) {
        String commentCount = commentCountText(entity.getId());
        return new DetailedPost(
            entity.getId(),
            entity.getTitle(),
            entity.getExcerpt(),
            entity.getCategoryName(),
            boardLabel(entity.getAuthorType()),
            entity.getPublishedAt().format(DATE_FORMATTER),
            entity.getViews(),
            commentCount,
            entity.getLikes(),
            entity.getReadTime(),
            entity.getCoverLabel(),
            entity.getCoverStyle(),
            parseTags(entity.getTagsText()),
            entity.getIntro(),
            new AuthorProfile(
                entity.getAuthorName(),
                entity.getAuthorTitle(),
                entity.getAuthorAvatarLabel(),
                entity.getAuthorFollowers(),
                String.valueOf(resolveAuthorArticleCount(entity))
            ),
            parseBlocks(entity.getContentText()),
            new PostStats(
                entity.getLikes(),
                entity.getFavorites(),
                commentCount,
                entity.getShares()
            )
        );
    }

    private List<String> parseTags(String tagsText) {
        if (tagsText == null || tagsText.isBlank()) {
            return List.of();
        }

        return List.of(tagsText.split(DEFAULT_TAG_DELIMITER)).stream()
            .map(String::trim)
            .filter(tag -> !tag.isBlank())
            .toList();
    }

    public String categoryName(String slug) {
        return categories.stream()
            .filter(item -> item.slug().equalsIgnoreCase(slug))
            .map(CategoryDto::name)
            .findFirst()
            .orElse("灵感笔记");
    }

    public String coverStyle(String tone) {
        Map<String, String> styles = new LinkedHashMap<>();
        styles.put("pink-cyan", "linear-gradient(135deg, #fb7299 0%, #ffb7cc 38%, #5ac8fa 100%)");
        styles.put("neon-night", "linear-gradient(135deg, #1f274f 0%, #6d3ecb 38%, #fb7299 100%)");
        styles.put("mint-wave", "linear-gradient(135deg, #2fc89f 0%, #78e4be 45%, #5ac8fa 100%)");
        return styles.getOrDefault(tone, styles.get("pink-cyan"));
    }

    private List<PostSummary> listSummariesByAuthorType(BlogPostEntity.AuthorType authorType) {
        return blogPostRepository.findAllByAuthorTypeOrderByPublishedAtDescIdDesc(authorType).stream()
            .map(this::toSummary)
            .toList();
    }

    private long resolveAuthorArticleCount(BlogPostEntity entity) {
        if (entity.getAuthorUserId() != null) {
            return blogPostRepository.countByAuthorUserId(entity.getAuthorUserId());
        }
        return entity.getAuthorType() == BlogPostEntity.AuthorType.ADMIN
            ? blogPostRepository.countByAuthorType(BlogPostEntity.AuthorType.ADMIN)
            : blogPostRepository.count();
    }

    private String boardLabel(BlogPostEntity.AuthorType authorType) {
        return authorType == BlogPostEntity.AuthorType.ADMIN ? "主站专栏" : "投稿广场";
    }

    private String commentCountText(Long postId) {
        return String.valueOf(blogCommentRepository.countByPost_Id(postId));
    }

    private List<ContentBlock> parseBlocks(String content) {
        List<ContentBlock> blocks = new ArrayList<>();
        List<String> listBuffer = new ArrayList<>();

        for (String rawLine : content.split("\\R")) {
            String line = rawLine.trim();
            if (line.isBlank()) {
                flushList(blocks, listBuffer);
                continue;
            }

            if (line.startsWith("##")) {
                flushList(blocks, listBuffer);
                blocks.add(new ContentBlock("heading", line.substring(2).trim(), null));
                continue;
            }

            if (line.startsWith(">")) {
                flushList(blocks, listBuffer);
                blocks.add(new ContentBlock("quote", line.substring(1).trim(), null));
                continue;
            }

            if (line.startsWith("- ")) {
                listBuffer.add(line.substring(2).trim());
                continue;
            }

            flushList(blocks, listBuffer);
            blocks.add(new ContentBlock("paragraph", line, null));
        }

        flushList(blocks, listBuffer);

        if (blocks.isEmpty()) {
            blocks.add(new ContentBlock("paragraph", content.trim(), null));
        }

        return blocks;
    }

    private void flushList(List<ContentBlock> blocks, List<String> listBuffer) {
        if (!listBuffer.isEmpty()) {
            blocks.add(new ContentBlock("list", null, List.copyOf(listBuffer)));
            listBuffer.clear();
        }
    }
}
