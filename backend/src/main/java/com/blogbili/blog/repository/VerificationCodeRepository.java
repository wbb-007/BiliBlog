package com.blogbili.blog.repository;

import com.blogbili.blog.entity.VerificationCodeEntity;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VerificationCodeRepository extends JpaRepository<VerificationCodeEntity, Long> {

    Optional<VerificationCodeEntity> findFirstByEmailAndUsedFalseOrderByCreatedAtDesc(String email);

    Optional<VerificationCodeEntity> findFirstByEmailAndModeAndUsedFalseOrderByCreatedAtDesc(String email, String mode);
}
