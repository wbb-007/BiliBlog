package com.blogbili.blog.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import java.time.LocalDate;

@Entity
@Table(name = "blog_post")
public class BlogPostEntity {

    public enum AuthorType {
        ADMIN,
        USER
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 160)
    private String title;

    @Column(nullable = false, length = 600)
    private String excerpt;

    @Column(nullable = false, length = 64)
    private String categorySlug;

    @Column(nullable = false, length = 64)
    private String categoryName;

    @Column(nullable = false)
    private LocalDate publishedAt;

    @Column(nullable = false, length = 32)
    private String views;

    @Column(nullable = false, length = 32)
    private String comments;

    @Column(nullable = false, length = 32)
    private String likes;

    @Column(nullable = false, length = 32)
    private String favorites;

    @Column(nullable = false, length = 32)
    private String shares;

    @Column(nullable = false, length = 32)
    private String readTime;

    @Column(nullable = false, length = 80)
    private String coverLabel;

    @Column(nullable = false, length = 255)
    private String coverStyle;

    @Column(nullable = false, length = 600)
    private String intro;

    @Column(nullable = false, length = 80)
    private String authorName;

    @Column(nullable = false, length = 80)
    private String authorTitle;

    @Column(nullable = false, length = 20)
    private String authorAvatarLabel;

    @Column(nullable = false, length = 32)
    private String authorFollowers;

    @Column(nullable = false, length = 32)
    private String authorArticles;

    private Long authorUserId;

    @jakarta.persistence.Enumerated(jakarta.persistence.EnumType.STRING)
    @Column(length = 16)
    private AuthorType authorType;

    @Column(nullable = false, length = 300)
    private String tagsText;

    @Lob
    @Column(nullable = false, columnDefinition = "LONGTEXT")
    private String contentText;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getExcerpt() {
        return excerpt;
    }

    public void setExcerpt(String excerpt) {
        this.excerpt = excerpt;
    }

    public String getCategorySlug() {
        return categorySlug;
    }

    public void setCategorySlug(String categorySlug) {
        this.categorySlug = categorySlug;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public LocalDate getPublishedAt() {
        return publishedAt;
    }

    public void setPublishedAt(LocalDate publishedAt) {
        this.publishedAt = publishedAt;
    }

    public String getViews() {
        return views;
    }

    public void setViews(String views) {
        this.views = views;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

    public String getLikes() {
        return likes;
    }

    public void setLikes(String likes) {
        this.likes = likes;
    }

    public String getFavorites() {
        return favorites;
    }

    public void setFavorites(String favorites) {
        this.favorites = favorites;
    }

    public String getShares() {
        return shares;
    }

    public void setShares(String shares) {
        this.shares = shares;
    }

    public String getReadTime() {
        return readTime;
    }

    public void setReadTime(String readTime) {
        this.readTime = readTime;
    }

    public String getCoverLabel() {
        return coverLabel;
    }

    public void setCoverLabel(String coverLabel) {
        this.coverLabel = coverLabel;
    }

    public String getCoverStyle() {
        return coverStyle;
    }

    public void setCoverStyle(String coverStyle) {
        this.coverStyle = coverStyle;
    }

    public String getIntro() {
        return intro;
    }

    public void setIntro(String intro) {
        this.intro = intro;
    }

    public String getAuthorName() {
        return authorName;
    }

    public void setAuthorName(String authorName) {
        this.authorName = authorName;
    }

    public String getAuthorTitle() {
        return authorTitle;
    }

    public void setAuthorTitle(String authorTitle) {
        this.authorTitle = authorTitle;
    }

    public String getAuthorAvatarLabel() {
        return authorAvatarLabel;
    }

    public void setAuthorAvatarLabel(String authorAvatarLabel) {
        this.authorAvatarLabel = authorAvatarLabel;
    }

    public String getAuthorFollowers() {
        return authorFollowers;
    }

    public void setAuthorFollowers(String authorFollowers) {
        this.authorFollowers = authorFollowers;
    }

    public String getAuthorArticles() {
        return authorArticles;
    }

    public void setAuthorArticles(String authorArticles) {
        this.authorArticles = authorArticles;
    }

    public Long getAuthorUserId() {
        return authorUserId;
    }

    public void setAuthorUserId(Long authorUserId) {
        this.authorUserId = authorUserId;
    }

    public AuthorType getAuthorType() {
        return authorType == null ? AuthorType.ADMIN : authorType;
    }

    public boolean hasStoredAuthorType() {
        return authorType != null;
    }

    public void setAuthorType(AuthorType authorType) {
        this.authorType = authorType;
    }

    public String getTagsText() {
        return tagsText;
    }

    public void setTagsText(String tagsText) {
        this.tagsText = tagsText;
    }

    public String getContentText() {
        return contentText;
    }

    public void setContentText(String contentText) {
        this.contentText = contentText;
    }
}
