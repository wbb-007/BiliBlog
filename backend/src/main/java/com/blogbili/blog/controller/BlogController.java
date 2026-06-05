package com.blogbili.blog.controller;

import com.blogbili.blog.model.CategoryDto;
import com.blogbili.blog.model.CommentCreateRequest;
import com.blogbili.blog.model.CommentDto;
import com.blogbili.blog.model.CommunityResponse;
import com.blogbili.blog.model.GardenResponse;
import com.blogbili.blog.model.HomeResponse;
import com.blogbili.blog.model.PostDetailResponse;
import com.blogbili.blog.model.ProfileResponse;
import com.blogbili.blog.model.PublishPostRequest;
import com.blogbili.blog.model.PublishPostResponse;
import com.blogbili.blog.service.BlogDataService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class BlogController {

    private final BlogDataService blogDataService;

    public BlogController(BlogDataService blogDataService) {
        this.blogDataService = blogDataService;
    }

    @GetMapping("/home")
    public HomeResponse home() {
        return blogDataService.getHome();
    }

    @GetMapping("/community")
    public CommunityResponse community() {
        return blogDataService.getCommunity();
    }

    @GetMapping("/garden")
    public GardenResponse garden() {
        return blogDataService.getGarden();
    }

    @GetMapping("/categories")
    public List<CategoryDto> categories() {
        return blogDataService.getCategories();
    }

    @GetMapping("/posts/{id}")
    public PostDetailResponse post(@PathVariable Long id) {
        return blogDataService.getPost(id);
    }

    @GetMapping("/posts/{id}/comments")
    public List<CommentDto> postComments(@PathVariable Long id) {
        return blogDataService.getPostComments(id);
    }

    @PostMapping("/posts/{id}/comments")
    public CommentDto addComment(
        @PathVariable Long id,
        @Valid @RequestBody CommentCreateRequest request,
        HttpServletRequest httpRequest
    ) {
        return blogDataService.addComment(id, request, httpRequest);
    }

    @GetMapping("/profile")
    public ProfileResponse profile() {
        return blogDataService.getProfile();
    }

    @PostMapping("/posts")
    public PublishPostResponse publish(
        @Valid @RequestBody PublishPostRequest request,
        HttpServletRequest httpRequest
    ) {
        return blogDataService.publish(request, httpRequest);
    }
}
