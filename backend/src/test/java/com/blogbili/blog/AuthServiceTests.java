package com.blogbili.blog;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.blogbili.blog.entity.UserEntity;
import com.blogbili.blog.model.EmailCodeRequest;
import com.blogbili.blog.model.RegisterRequest;
import com.blogbili.blog.repository.UserRepository;
import com.blogbili.blog.service.AuthService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.server.ResponseStatusException;

@SpringBootTest
class AuthServiceTests {

    @Autowired
    private AuthService authService;

    @Autowired
    private UserRepository userRepository;

    @Test
    void firstAdminCanBindRealEmailThenRegistrationCloses() {
        var codeResponse = authService.sendCode(new EmailCodeRequest("owner@example.com", "REGISTER"));
        assertThat(codeResponse.devCode()).isNotBlank();

        var authResponse = authService.register(new RegisterRequest(
            "owner@example.com",
            codeResponse.devCode(),
            "Owner",
            "OwnerPass123"
        ));

        assertThat(authResponse.user().email()).isEqualTo("owner@example.com");
        assertThat(authResponse.user().role()).isEqualTo(UserEntity.Role.ADMIN.name());

        assertThatThrownBy(() -> authService.sendCode(new EmailCodeRequest("other@example.com", "REGISTER")))
            .isInstanceOf(ResponseStatusException.class)
            .hasMessageContaining("403 FORBIDDEN");
    }
}
