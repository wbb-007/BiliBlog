package com.blogbili.blog.service;

import com.blogbili.blog.entity.AnnouncementEntity;
import com.blogbili.blog.entity.AlbumPhotoEntity;
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
import com.blogbili.blog.model.GardenLinkDto;
import com.blogbili.blog.model.GardenNoteDto;
import com.blogbili.blog.model.GardenPhotoDto;
import com.blogbili.blog.model.GardenResponse;
import com.blogbili.blog.model.GardenToolDto;
import com.blogbili.blog.model.HomeResponse;
import com.blogbili.blog.model.LatestCommentDto;
import com.blogbili.blog.model.Metric;
import com.blogbili.blog.model.PostDetailResponse;
import com.blogbili.blog.model.PostStats;
import com.blogbili.blog.model.PostSummary;
import com.blogbili.blog.model.ProfileIdentity;
import com.blogbili.blog.model.ProfileResponse;
import com.blogbili.blog.model.PublishPostRequest;
import com.blogbili.blog.model.PublishPostResponse;
import com.blogbili.blog.model.TagStatDto;
import com.blogbili.blog.model.TimelineItem;
import com.blogbili.blog.repository.AnnouncementRepository;
import com.blogbili.blog.repository.AlbumPhotoRepository;
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class BlogDataService {

    private static final String DEFAULT_TAG_DELIMITER = "\\|\\|";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_DATE;
    private static final DateTimeFormatter COMMENT_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    private static final Pattern MARKDOWN_IMAGE_PATTERN = Pattern.compile("^!\\[([^]]*)]\\(([^)]+)\\)$");
    private static final int ANIME_AVATAR_POOL_SIZE = 100;

    private final BlogPostRepository blogPostRepository;
    private final BlogCommentRepository blogCommentRepository;
    private final AnnouncementRepository announcementRepository;
    private final AlbumPhotoRepository albumPhotoRepository;
    private final UserRepository userRepository;
    private final AuthService authService;
    private final AdminService adminService;
    private final ProfileSettingsService profileSettingsService;

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
        "",
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
        AlbumPhotoRepository albumPhotoRepository,
        UserRepository userRepository,
        AuthService authService,
        AdminService adminService,
        ProfileSettingsService profileSettingsService
    ) {
        this.blogPostRepository = blogPostRepository;
        this.blogCommentRepository = blogCommentRepository;
        this.announcementRepository = announcementRepository;
        this.albumPhotoRepository = albumPhotoRepository;
        this.userRepository = userRepository;
        this.authService = authService;
        this.adminService = adminService;
        this.profileSettingsService = profileSettingsService;
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
            effectiveCreatorProfile(),
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
            ),
            latestComments(),
            tagCloud()
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

    public GardenResponse getGarden() {
        return new GardenResponse(
            List.of(
                new Metric("微言", "6"),
                new Metric("友链", "4"),
                new Metric("工具", "8"),
                new Metric("旅拍", "5")
            ),
            List.of(
                new GardenNoteDto(
                    "把 AI 笔记写成可复用流程",
                    "今天把提示词模板拆成输入、约束、输出、检查四段，发现很多教程其实可以变成自己的工作流卡片。",
                    "创作中",
                    "2026-06-05 13:20",
                    "pink"
                ),
                new GardenNoteDto(
                    "RAG 不只是向量库",
                    "检索质量更像整理书架：标题、摘要、标签和分段，比单纯堆模型参数更重要。",
                    "学习札记",
                    "2026-06-04 22:10",
                    "cyan"
                ),
                new GardenNoteDto(
                    "给博客留一点生活感",
                    "文章负责沉淀知识，花园负责保存闪念、朋友、图片和小工具。两者放在一起，站点会更像一个人。",
                    "站点设计",
                    "2026-06-03 18:45",
                    "mint"
                )
            ),
            List.of(
                new GardenLinkDto("Poetize", "诗意栖居的数字花园灵感来源", "https://gitee.com/littledokey/poetize", "", "开源博客"),
                new GardenLinkDto("AI Studio", "收藏 AI 写作、RAG 和提示词实践", "#", "", "AI 教程"),
                new GardenLinkDto("Design Log", "记录界面、配色和交互动效", "#", "", "设计观察"),
                new GardenLinkDto("Frontend Lab", "前端实验与组件灵感库", "#", "", "技术实验")
            ),
            List.of(
                new GardenToolDto("提示词卡片", "把常用 prompt 保存成结构化模板，写教程时直接复用。", "AI", "整理模板", "#fb7299"),
                new GardenToolDto("RAG 清单", "按资料清洗、切分、召回、重排、回答检查拆任务。", "知识库", "查看流程", "#5ac8fa"),
                new GardenToolDto("页面走查", "发布前检查移动端、空状态、按钮文案和图片尺寸。", "前端", "开始检查", "#2fc89f"),
                new GardenToolDto("封面灵感", "为文章生成配色方向、标题字重和封面标签。", "创作", "生成方向", "#6d3ecb")
            ),
            gardenPhotos(),
            latestComments()
        );
    }

    public List<CategoryDto> getCategories() {
        return categories;
    }

    private List<GardenPhotoDto> gardenPhotos() {
        List<GardenPhotoDto> uploaded = albumPhotoRepository.findAllByActiveTrueOrderByCreatedAtDesc().stream()
            .map(this::toGardenPhoto)
            .toList();
        if (!uploaded.isEmpty()) {
            return uploaded;
        }
        return List.of(
            new GardenPhotoDto(
                "夜色下的工作台",
                "Shanghai",
                "",
                "一杯咖啡、一块屏幕，以及还没写完的 AI 教程。",
                "linear-gradient(135deg, #1f274f 0%, #6d3ecb 45%, #fb7299 100%)"
            ),
            new GardenPhotoDto(
                "周末的灵感散步",
                "Suzhou",
                "",
                "把生活里的颜色收集回来，变成博客里的封面。",
                "linear-gradient(135deg, #2fc89f 0%, #78e4be 45%, #5ac8fa 100%)"
            ),
            new GardenPhotoDto(
                "教程草稿墙",
                "Home Studio",
                "",
                "提示词、RAG、前端评审，先贴成一面墙，再慢慢写成文章。",
                "linear-gradient(135deg, #fb7299 0%, #ffb7cc 40%, #5ac8fa 100%)"
            )
        );
    }

    private GardenPhotoDto toGardenPhoto(AlbumPhotoEntity entity) {
        return new GardenPhotoDto(
            entity.getTitle(),
            entity.getLocation(),
            entity.getImageUrl(),
            entity.getCaption(),
            entity.getColor()
        );
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
                commentAvatarUrl(comment),
                comment.getCreatedAt().format(COMMENT_TIME_FORMATTER),
                commentIpLocation(comment),
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
        comment.setUserAvatarUrl(generatedAnimeAvatar(nickname + "-" + System.nanoTime()));
        comment.setIpAddress(clientIp(httpRequest));
        comment.setIpLocation(resolveIpLocation(comment.getIpAddress()));
        comment.setContent(request.content().trim());

        BlogCommentEntity saved = blogCommentRepository.save(comment);

        return new CommentDto(
            saved.getId(),
            saved.getUserNickname(),
            saved.getUserInitial(),
            commentAvatarUrl(saved),
            saved.getCreatedAt().format(COMMENT_TIME_FORMATTER),
            commentIpLocation(saved),
            saved.getContent()
        );
    }

    public ProfileResponse getProfile() {
        List<PostSummary> summaries = listAdminPostSummaries();
        var profileSettings = profileSettingsService.getSettings();
        return new ProfileResponse(
            new ProfileIdentity(
                profileSettings.name(),
                profileSettings.headline(),
                profileSettings.bio(),
                profileSettings.avatarLabel(),
                profileAvatarUrl(profileSettings.avatarUrl()),
                profileSettings.bannerStyle(),
                List.of(
                    new Metric("粉丝", profileSettings.followers()),
                    new Metric("关注", "342"),
                    new Metric("获赞", profileSettings.likes()),
                    new Metric("文章", String.valueOf(blogPostRepository.countByAuthorType(BlogPostEntity.AuthorType.ADMIN)))
                ),
                profileSettings.tags(),
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
        post.setCoverStyle(coverStyle(request.coverTone(), request.coverImageUrl()));
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
        String coverStyle = coverStyle(request.coverTone(), request.coverImageUrl());
        return toSummary(adminService.updatePost(id, request, categoryName, coverStyle));
    }

    private PostSummary toSummary(BlogPostEntity entity) {
        String commentCount = commentCountText(entity.getId());
        var profileSettings = profileSettingsService.getSettings();
        String authorAvatarUrl = entity.getAuthorType() == BlogPostEntity.AuthorType.ADMIN
            ? profileAvatarUrl(profileSettings.avatarUrl())
            : generatedAnimeAvatar(entity.getAuthorName());
        String authorName = entity.getAuthorType() == BlogPostEntity.AuthorType.ADMIN
            ? profileSettings.name()
            : entity.getAuthorName();
        String authorAvatarLabel = entity.getAuthorType() == BlogPostEntity.AuthorType.ADMIN
            ? profileSettings.avatarLabel()
            : entity.getAuthorAvatarLabel();
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
            authorName,
            authorAvatarLabel,
            authorAvatarUrl
        );
    }

    private DetailedPost toDetailedPost(BlogPostEntity entity) {
        String commentCount = commentCountText(entity.getId());
        var profileSettings = profileSettingsService.getSettings();
        String authorAvatarUrl = entity.getAuthorType() == BlogPostEntity.AuthorType.ADMIN
            ? profileAvatarUrl(profileSettings.avatarUrl())
            : generatedAnimeAvatar(entity.getAuthorName());
        String authorName = entity.getAuthorType() == BlogPostEntity.AuthorType.ADMIN
            ? profileSettings.name()
            : entity.getAuthorName();
        String authorTitle = entity.getAuthorType() == BlogPostEntity.AuthorType.ADMIN
            ? profileSettings.headline()
            : entity.getAuthorTitle();
        String authorAvatarLabel = entity.getAuthorType() == BlogPostEntity.AuthorType.ADMIN
            ? profileSettings.avatarLabel()
            : entity.getAuthorAvatarLabel();
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
                authorName,
                authorTitle,
                authorAvatarLabel,
                authorAvatarUrl,
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

    private List<LatestCommentDto> latestComments() {
        return blogCommentRepository.findAllWithPostOrderByCreatedAtDesc().stream()
            .limit(10)
            .map(comment -> new LatestCommentDto(
                comment.getId(),
                comment.getPost() == null ? null : comment.getPost().getId(),
                comment.getPost() == null ? "已删除文章" : comment.getPost().getTitle(),
                comment.getUserNickname(),
                comment.getUserInitial(),
                commentAvatarUrl(comment),
                comment.getContent(),
                comment.getCreatedAt().format(COMMENT_TIME_FORMATTER)
            ))
            .toList();
    }

    private List<TagStatDto> tagCloud() {
        Map<String, Long> counts = new LinkedHashMap<>();
        blogPostRepository.findAllByOrderByPublishedAtDescIdDesc().forEach(post ->
            parseTags(post.getTagsText()).forEach(tag -> counts.merge(tag, 1L, Long::sum))
        );

        return counts.entrySet().stream()
            .sorted((left, right) -> {
                int countCompare = Long.compare(right.getValue(), left.getValue());
                return countCompare != 0 ? countCompare : left.getKey().compareToIgnoreCase(right.getKey());
            })
            .limit(18)
            .map(entry -> new TagStatDto(entry.getKey(), entry.getValue()))
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

    public String coverStyle(String tone, String coverImageUrl) {
        if (coverImageUrl != null && !coverImageUrl.isBlank()) {
            String safeUrl = coverImageUrl.trim()
                .replace("\\", "\\\\")
                .replace("\"", "\\\"");
            return "url(\"" + safeUrl + "\") center / cover no-repeat";
        }
        return coverStyle(tone);
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

    private CreatorProfile effectiveCreatorProfile() {
        var settings = profileSettingsService.getSettings();
        return new CreatorProfile(
            settings.name(),
            settings.headline(),
            settings.bio(),
            settings.avatarLabel(),
            profileAvatarUrl(settings.avatarUrl()),
            settings.bannerStyle(),
            settings.followers(),
            settings.likes(),
            creatorProfile.posts(),
            settings.tags()
        );
    }

    private String commentAvatarUrl(BlogCommentEntity comment) {
        if (comment.getUserAvatarUrl() != null && !comment.getUserAvatarUrl().isBlank()) {
            return comment.getUserAvatarUrl();
        }
        return generatedAnimeAvatar(comment.getUserNickname() + "-" + comment.getId());
    }

    private String commentIpLocation(BlogCommentEntity comment) {
        if (comment.getIpLocation() != null && !comment.getIpLocation().isBlank()) {
            return comment.getIpLocation();
        }
        return resolveIpLocation(comment.getIpAddress());
    }

    private String profileAvatarUrl() {
        return profileAvatarUrl(profileSettingsService.getSettings().avatarUrl());
    }

    private String profileAvatarUrl(String configuredUrl) {
        if (configuredUrl != null && !configuredUrl.isBlank()) {
            return configuredUrl;
        }
        return generatedAnimeAvatar(creatorProfile.name());
    }

    private String generatedAnimeAvatar(String seed) {
        int poolIndex = Math.floorMod(seed == null ? 0 : seed.hashCode(), ANIME_AVATAR_POOL_SIZE) + 1;
        int imageId = switch (poolIndex) {
            case 1 -> 43078;
            case 2 -> 43074;
            case 3 -> 43077;
            case 4 -> 43073;
            case 5 -> 43062;
            case 6 -> 43067;
            case 7 -> 43066;
            case 8 -> 42872;
            case 9 -> 42968;
            case 10 -> 42280;
            case 11 -> 24695;
            case 12 -> 27301;
            case 13 -> 25942;
            case 14 -> 33959;
            case 15 -> 32331;
            case 16 -> 38682;
            case 17 -> 36855;
            case 18 -> 34883;
            case 19 -> 39059;
            case 20 -> 43068;
            case 21 -> 43039;
            case 22 -> 43032;
            case 23 -> 42990;
            case 24 -> 43038;
            case 25 -> 43037;
            case 26 -> 43036;
            case 27 -> 43030;
            case 28 -> 43079;
            case 29 -> 43031;
            case 30 -> 42989;
            case 31 -> 42948;
            case 32 -> 43014;
            case 33 -> 43013;
            case 34 -> 43011;
            case 35 -> 43010;
            case 36 -> 43009;
            case 37 -> 43008;
            case 38 -> 43003;
            case 39 -> 42999;
            case 40 -> 42991;
            case 41 -> 42987;
            case 42 -> 42949;
            case 43 -> 42965;
            case 44 -> 42967;
            case 45 -> 42946;
            case 46 -> 42966;
            case 47 -> 42962;
            case 48 -> 42937;
            case 49 -> 42954;
            case 50 -> 42944;
            case 51 -> 42947;
            case 52 -> 42945;
            case 53 -> 42963;
            case 54 -> 42932;
            case 55 -> 42929;
            case 56 -> 42873;
            case 57 -> 42856;
            case 58 -> 42855;
            case 59 -> 42938;
            case 60 -> 42925;
            case 61 -> 42924;
            case 62 -> 42889;
            case 63 -> 42833;
            case 64 -> 42743;
            case 65 -> 42676;
            case 66 -> 42903;
            case 67 -> 42901;
            case 68 -> 42898;
            case 69 -> 42897;
            case 70 -> 42896;
            case 71 -> 42894;
            case 72 -> 42893;
            case 73 -> 42888;
            case 74 -> 42955;
            case 75 -> 42871;
            case 76 -> 42869;
            case 77 -> 42864;
            case 78 -> 42961;
            case 79 -> 42837;
            case 80 -> 42836;
            case 81 -> 42834;
            case 82 -> 42831;
            case 83 -> 42757;
            case 84 -> 42721;
            case 85 -> 42707;
            case 86 -> 42680;
            case 87 -> 42677;
            case 88 -> 42668;
            case 89 -> 42657;
            case 90 -> 42615;
            case 91 -> 42592;
            case 92 -> 42573;
            case 93 -> 42784;
            case 94 -> 42782;
            case 95 -> 42780;
            case 96 -> 42779;
            case 97 -> 42778;
            case 98 -> 42776;
            case 99 -> 42775;
            default -> 42774;
        };
        return "/backup-images/anime/anime-%03d-%d.jpg".formatted(poolIndex, imageId);
    }

    private String clientIp(HttpServletRequest request) {
        String[] headers = {
            "X-Forwarded-For",
            "X-Real-IP",
            "Proxy-Client-IP",
            "WL-Proxy-Client-IP"
        };
        for (String header : headers) {
            String value = request.getHeader(header);
            if (value != null && !value.isBlank() && !"unknown".equalsIgnoreCase(value)) {
                return value.split(",")[0].trim();
            }
        }
        return request.getRemoteAddr();
    }

    private String resolveIpLocation(String ip) {
        if (ip == null || ip.isBlank()) {
            return "IP属地：未知";
        }
        if ("127.0.0.1".equals(ip) || "0:0:0:0:0:0:0:1".equals(ip) || "::1".equals(ip)) {
            return "IP属地：本机";
        }
        if (ip.startsWith("10.") || ip.startsWith("192.168.") || isPrivate172Ip(ip)) {
            return "IP属地：内网";
        }
        return "IP属地：未知";
    }

    private boolean isPrivate172Ip(String ip) {
        if (!ip.startsWith("172.")) {
            return false;
        }
        String[] parts = ip.split("\\.");
        if (parts.length < 2) {
            return false;
        }
        try {
            int second = Integer.parseInt(parts[1]);
            return second >= 16 && second <= 31;
        } catch (NumberFormatException error) {
            return false;
        }
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

            Matcher imageMatcher = MARKDOWN_IMAGE_PATTERN.matcher(line);
            if (imageMatcher.matches()) {
                flushList(blocks, listBuffer);
                blocks.add(new ContentBlock("image", imageMatcher.group(1).trim(), List.of(imageMatcher.group(2).trim())));
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
