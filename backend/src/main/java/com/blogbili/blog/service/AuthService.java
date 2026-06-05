package com.blogbili.blog.service;

import com.blogbili.blog.entity.AuthSessionEntity;
import com.blogbili.blog.entity.UserEntity;
import com.blogbili.blog.entity.VerificationCodeEntity;
import com.blogbili.blog.model.AuthResponse;
import com.blogbili.blog.model.EmailCodeRequest;
import com.blogbili.blog.model.LoginRequest;
import com.blogbili.blog.model.RegisterRequest;
import com.blogbili.blog.model.ResetPasswordRequest;
import com.blogbili.blog.model.SendCodeResponse;
import com.blogbili.blog.model.UserSessionDto;
import com.blogbili.blog.repository.AuthSessionRepository;
import com.blogbili.blog.repository.UserRepository;
import com.blogbili.blog.repository.VerificationCodeRepository;
import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AuthService {

    private static final String PURPOSE_REGISTER = "REGISTER";
    private static final String PURPOSE_RESET_PASSWORD = "RESET_PASSWORD";
    private static final String DEFAULT_ADMIN_EMAIL = "admin@biliblog.local";

    private final UserRepository userRepository;
    private final VerificationCodeRepository verificationCodeRepository;
    private final AuthSessionRepository authSessionRepository;
    private final EmailCodeSender emailCodeSender;
    private final PasswordCodec passwordCodec;

    @Value("${blog.admin.nickname}")
    private String adminNickname;

    @Value("${blog.auth.code-minutes:10}")
    private long codeMinutes;

    @Value("${blog.auth.session-days:7}")
    private long sessionDays;

    @Value("${blog.mail.debug-return-code:true}")
    private boolean debugReturnCode;

    public AuthService(
        UserRepository userRepository,
        VerificationCodeRepository verificationCodeRepository,
        AuthSessionRepository authSessionRepository,
        EmailCodeSender emailCodeSender,
        PasswordCodec passwordCodec
    ) {
        this.userRepository = userRepository;
        this.verificationCodeRepository = verificationCodeRepository;
        this.authSessionRepository = authSessionRepository;
        this.emailCodeSender = emailCodeSender;
        this.passwordCodec = passwordCodec;
    }

    @Transactional
    public SendCodeResponse sendCode(EmailCodeRequest request) {
        String normalizedEmail = normalizeEmail(request.email());
        String purpose = normalizePurpose(request.purpose());
        authSessionRepository.deleteByExpiresAtBefore(LocalDateTime.now());
        String code = String.format("%06d", ThreadLocalRandom.current().nextInt(0, 1_000_000));
        UserEntity existingUser = userRepository.findByEmail(normalizedEmail).orElse(null);

        if (PURPOSE_REGISTER.equals(purpose)) {
            ensureAdminRegisterAllowed(normalizedEmail, existingUser);
            if (existingUser != null && existingUser.getRole() != UserEntity.Role.ADMIN) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "当前系统仅允许管理员账号注册");
            }
        } else if (PURPOSE_RESET_PASSWORD.equals(purpose)) {
            if (existingUser == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "管理员账号尚未初始化，请先注册");
            }
            if (existingUser.getRole() != UserEntity.Role.ADMIN) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "当前系统仅允许管理员账号操作");
            }
            if (existingUser.getStatus() == UserEntity.Status.DISABLED) {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "账号已被停用，请联系管理员");
            }
        } else {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "不支持的验证码用途");
        }

        VerificationCodeEntity entity = new VerificationCodeEntity();
        entity.setEmail(normalizedEmail);
        entity.setCode(code);
        entity.setMode(purpose);
        entity.setExpiresAt(LocalDateTime.now().plusMinutes(codeMinutes));
        entity.setUsed(false);
        verificationCodeRepository.save(entity);

        boolean sent = emailCodeSender.send(normalizedEmail, code, PURPOSE_REGISTER.equals(purpose) ? "注册" : "找回密码");
        if (!sent && !debugReturnCode) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "SMTP 未配置或邮件发送失败，请先完成邮箱服务配置");
        }

        String devCode = (!sent && debugReturnCode) ? code : null;
        return new SendCodeResponse(
            purpose,
            sent ? "验证码已发送，请前往邮箱查收。" : "当前处于调试模式，验证码已直接返回。",
            devCode
        );
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        String normalizedEmail = normalizeEmail(request.email());
        UserEntity existingUser = userRepository.findByEmail(normalizedEmail).orElse(null);
        ensureAdminRegisterAllowed(normalizedEmail, existingUser);

        validateCode(normalizedEmail, request.code(), PURPOSE_REGISTER);
        String rawPassword = validatePassword(request.password());
        UserEntity user = userRepository.findByEmail(normalizedEmail)
            .map(existing -> updateAdminUser(existing, request.nickname(), rawPassword))
            .orElseGet(() -> rebindDefaultAdmin(normalizedEmail, request.nickname(), rawPassword)
                .orElseGet(() -> createAdminUser(normalizedEmail, request.nickname(), rawPassword)));
        return createSessionResponse(user, "管理员注册并登录成功");
    }

    @Transactional
    public AuthResponse login(LoginRequest request) {
        String normalizedEmail = normalizeEmail(request.email());
        String rawPassword = request.password();
        UserEntity user = userRepository.findByEmail(normalizedEmail)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "邮箱或密码错误"));

        if (user.getRole() != UserEntity.Role.ADMIN) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "当前账号不是管理员");
        }
        if (user.getStatus() == UserEntity.Status.DISABLED) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "账号已被停用，请联系管理员");
        }
        if (!passwordCodec.matches(rawPassword, user.getPasswordHash())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "邮箱或密码错误");
        }

        return createSessionResponse(user, "登录成功");
    }

    @Transactional
    public AuthResponse resetPassword(ResetPasswordRequest request) {
        String normalizedEmail = normalizeEmail(request.email());
        UserEntity user = userRepository.findByEmail(normalizedEmail)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "管理员账号尚未初始化，请先注册"));

        if (user.getRole() != UserEntity.Role.ADMIN) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "当前账号不是管理员");
        }

        validateCode(normalizedEmail, request.code(), PURPOSE_RESET_PASSWORD);
        user.setPasswordHash(passwordCodec.encode(validatePassword(request.password())));
        if (user.getStatus() == UserEntity.Status.DISABLED) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "账号已被停用，请联系管理员");
        }
        userRepository.save(user);
        authSessionRepository.deleteByUser_Id(user.getId());
        return createSessionResponse(user, "密码已重置并登录成功");
    }

    public AuthResponse me(HttpServletRequest request) {
        CurrentUser currentUser = requireUser(request);
        return new AuthResponse("ok", tokenFromRequest(request), new UserSessionDto(
            currentUser.id(),
            currentUser.email(),
            currentUser.nickname(),
            currentUser.role().name()
        ));
    }

    @Transactional
    public void logout(HttpServletRequest request) {
        String token = tokenFromRequest(request);
        if (token != null) {
            authSessionRepository.deleteByToken(token);
        }
    }

    public CurrentUser requireUser(HttpServletRequest request) {
        String token = tokenFromRequest(request);
        if (token == null || token.isBlank()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "请先登录");
        }

        AuthSessionEntity session = authSessionRepository.findByToken(token)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "登录状态已失效"));

        if (session.getExpiresAt().isBefore(LocalDateTime.now())) {
            authSessionRepository.delete(session);
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "登录状态已过期");
        }

        UserEntity user = session.getUser();
        if (user.getStatus() == UserEntity.Status.DISABLED) {
            authSessionRepository.delete(session);
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "账号已被停用");
        }

        return new CurrentUser(user.getId(), user.getEmail(), user.getNickname(), user.getRole());
    }

    public CurrentUser requireAdmin(HttpServletRequest request) {
        CurrentUser user = requireUser(request);
        if (user.role() != UserEntity.Role.ADMIN) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "仅管理员可访问");
        }
        return user;
    }

    private UserEntity createAdminUser(String email, String nickname, String rawPassword) {
        UserEntity user = new UserEntity();
        user.setEmail(email);
        user.setNickname(resolveNickname(nickname));
        user.setPasswordHash(passwordCodec.encode(rawPassword));
        user.setRole(UserEntity.Role.ADMIN);
        user.setStatus(UserEntity.Status.ACTIVE);
        return userRepository.save(user);
    }

    private UserEntity updateAdminUser(UserEntity user, String nickname, String rawPassword) {
        user.setNickname(resolveNickname(nickname));
        user.setPasswordHash(passwordCodec.encode(rawPassword));
        user.setRole(UserEntity.Role.ADMIN);
        user.setStatus(UserEntity.Status.ACTIVE);
        return userRepository.save(user);
    }

    private Optional<UserEntity> rebindDefaultAdmin(String email, String nickname, String rawPassword) {
        return rebindableDefaultAdmin().map(user -> {
            user.setEmail(email);
            return updateAdminUser(user, nickname, rawPassword);
        });
    }

    private AuthResponse createSessionResponse(UserEntity user, String message) {
        user.setLastLoginAt(LocalDateTime.now());
        userRepository.save(user);

        AuthSessionEntity session = new AuthSessionEntity();
        session.setToken(UUID.randomUUID().toString().replace("-", ""));
        session.setUser(user);
        session.setExpiresAt(LocalDateTime.now().plusDays(sessionDays));
        authSessionRepository.save(session);

        return new AuthResponse(
            message,
            session.getToken(),
            toUserSession(user)
        );
    }

    private UserSessionDto toUserSession(UserEntity user) {
        return new UserSessionDto(user.getId(), user.getEmail(), user.getNickname(), user.getRole().name());
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }

    private String normalizePurpose(String purpose) {
        return purpose.trim().toUpperCase(Locale.ROOT);
    }

    private boolean isDefaultAdminEmail(String email) {
        return normalizeEmail(email).equals(DEFAULT_ADMIN_EMAIL);
    }

    private String resolveNickname(String nickname) {
        if (nickname != null && !nickname.trim().isBlank()) {
            return nickname.trim();
        }

        return adminNickname;
    }

    private void ensureAdminRegisterAllowed(String email, UserEntity existingUser) {
        if (existingUser != null) {
            if (existingUser.getRole() == UserEntity.Role.ADMIN) {
                return;
            }
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "当前系统仅允许管理员账号注册");
        }

        List<UserEntity> admins = adminUsers();
        if (admins.isEmpty() || rebindableDefaultAdmin(admins).isPresent()) {
            return;
        }

        throw new ResponseStatusException(HttpStatus.FORBIDDEN, "管理员邮箱已初始化，请使用现有管理员邮箱登录或找回密码");
    }

    private Optional<UserEntity> rebindableDefaultAdmin() {
        return rebindableDefaultAdmin(adminUsers());
    }

    private Optional<UserEntity> rebindableDefaultAdmin(List<UserEntity> admins) {
        if (admins.size() != 1) {
            return Optional.empty();
        }

        UserEntity admin = admins.get(0);
        return isDefaultAdminEmail(admin.getEmail()) ? Optional.of(admin) : Optional.empty();
    }

    private List<UserEntity> adminUsers() {
        return userRepository.findByRoleOrderByIdAsc(UserEntity.Role.ADMIN);
    }

    private void validateCode(String email, String code, String purpose) {
        VerificationCodeEntity verificationCode = verificationCodeRepository
            .findFirstByEmailAndModeAndUsedFalseOrderByCreatedAtDesc(email, purpose)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "请先获取验证码"));

        if (verificationCode.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "验证码已过期，请重新获取");
        }

        if (!verificationCode.getCode().equals(code.trim())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "验证码错误");
        }

        verificationCode.setUsed(true);
        verificationCodeRepository.save(verificationCode);
    }

    private String validatePassword(String password) {
        if (password == null || password.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "密码不能为空");
        }

        if (password.length() < 8 || password.length() > 72) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "密码长度需在 8 到 72 位之间");
        }

        return password;
    }

    private String tokenFromRequest(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (header == null || header.isBlank()) {
            return null;
        }

        if (header.startsWith("Bearer ")) {
            return header.substring(7).trim();
        }

        return header.trim();
    }
}
