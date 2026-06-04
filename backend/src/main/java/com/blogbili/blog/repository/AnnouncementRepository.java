package com.blogbili.blog.repository;

import com.blogbili.blog.entity.AnnouncementEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AnnouncementRepository extends JpaRepository<AnnouncementEntity, Long> {

    List<AnnouncementEntity> findAllByOrderByCreatedAtDesc();
}
