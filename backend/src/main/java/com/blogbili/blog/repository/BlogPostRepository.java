package com.blogbili.blog.repository;

import com.blogbili.blog.entity.BlogPostEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BlogPostRepository extends JpaRepository<BlogPostEntity, Long> {

    List<BlogPostEntity> findAllByOrderByPublishedAtDescIdDesc();

    List<BlogPostEntity> findAllByAuthorTypeOrderByPublishedAtDescIdDesc(BlogPostEntity.AuthorType authorType);

    List<BlogPostEntity> findAllByAuthorUserId(Long authorUserId);

    long countByAuthorType(BlogPostEntity.AuthorType authorType);

    long countByAuthorUserId(Long authorUserId);
}
