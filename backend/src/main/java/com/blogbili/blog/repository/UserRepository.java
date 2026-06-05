package com.blogbili.blog.repository;

import com.blogbili.blog.entity.UserEntity;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<UserEntity, Long> {

    Optional<UserEntity> findByEmail(String email);

    List<UserEntity> findByRoleOrderByIdAsc(UserEntity.Role role);
}
