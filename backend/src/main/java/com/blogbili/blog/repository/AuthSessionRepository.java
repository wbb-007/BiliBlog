package com.blogbili.blog.repository;

import com.blogbili.blog.entity.AuthSessionEntity;
import java.time.LocalDateTime;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuthSessionRepository extends JpaRepository<AuthSessionEntity, Long> {

    Optional<AuthSessionEntity> findByToken(String token);

    void deleteByToken(String token);

    void deleteByExpiresAtBefore(LocalDateTime expiresAt);

    void deleteByUser_Id(Long userId);
}
