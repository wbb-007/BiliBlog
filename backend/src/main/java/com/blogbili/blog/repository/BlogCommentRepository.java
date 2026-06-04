package com.blogbili.blog.repository;

import com.blogbili.blog.entity.BlogCommentEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BlogCommentRepository extends JpaRepository<BlogCommentEntity, Long> {

    List<BlogCommentEntity> findAllByPost_IdOrderByCreatedAtDesc(Long postId);

    long countByPost_Id(Long postId);

    void deleteByPost_Id(Long postId);

    long countByUser_Id(Long userId);

    void deleteByUser_Id(Long userId);
}
