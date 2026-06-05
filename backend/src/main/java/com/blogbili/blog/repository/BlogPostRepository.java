package com.blogbili.blog.repository;

import com.blogbili.blog.entity.BlogPostEntity;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BlogPostRepository extends JpaRepository<BlogPostEntity, Long> {

    List<BlogPostEntity> findAllByOrderByPublishedAtDescIdDesc();

    List<BlogPostEntity> findAllByAuthorTypeOrderByPublishedAtDescIdDesc(BlogPostEntity.AuthorType authorType);

    List<BlogPostEntity> findAllByAuthorUserId(Long authorUserId);

    boolean existsByTitle(String title);

    Optional<BlogPostEntity> findByTitle(String title);

    long countByAuthorType(BlogPostEntity.AuthorType authorType);

    long countByAuthorUserId(Long authorUserId);
}
