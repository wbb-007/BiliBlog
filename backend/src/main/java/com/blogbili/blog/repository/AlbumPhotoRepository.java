package com.blogbili.blog.repository;

import com.blogbili.blog.entity.AlbumPhotoEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AlbumPhotoRepository extends JpaRepository<AlbumPhotoEntity, Long> {

    List<AlbumPhotoEntity> findAllByOrderByCreatedAtDesc();

    List<AlbumPhotoEntity> findAllByActiveTrueOrderByCreatedAtDesc();
}
