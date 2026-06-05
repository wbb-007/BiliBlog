package com.blogbili.blog.repository;

import com.blogbili.blog.entity.SiteSettingEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SiteSettingRepository extends JpaRepository<SiteSettingEntity, String> {
}
