package me.melkx.routeplanner.infrastructure.security.service;

import lombok.extern.slf4j.Slf4j;
import me.melkx.routeplanner.infrastructure.security.dto.ChangePasswordRequestDto;
import me.melkx.routeplanner.infrastructure.security.dto.JwtTokenPairResponseDto;
import me.melkx.routeplanner.infrastructure.security.dto.LoginRequestDto;
import me.melkx.routeplanner.infrastructure.security.dto.RegisterRequestDto;
import me.melkx.routeplanner.infrastructure.security.exception.*;
import me.melkx.routeplanner.module.user.UserEntity;
import me.melkx.routeplanner.module.user.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
public class JwtAuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtSessionService sessionService;

    @Autowired
    public JwtAuthService(UserRepository userRepository,
                          PasswordEncoder passwordEncoder,
                          JwtSessionService sessionService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.sessionService = sessionService;
    }

    @Transactional
    public JwtTokenPairResponseDto register(RegisterRequestDto request) {
        log.debug("Registration attempt, email={}", maskEmail(request.email()));

        if (userRepository.existsEmail(request.email())) {
            log.warn("Registration failed: email already exists, email={}", maskEmail(request.email()));
            throw new EmailAlreadyExistsException();
        }

        UserEntity user = UserEntity.builder()
                .email(request.email())
                .passwordHash(passwordEncoder.encode(request.password()))
                .build();

        userRepository.save(user);
        log.info("User registered, userId={}", user.getId());

        JwtTokenPairResponseDto tokens = sessionService.newSession(user.getId());
        return tokens;
    }

    public JwtTokenPairResponseDto login(LoginRequestDto request) {
        log.debug("Login attempt, email={}", maskEmail(request.email()));

        UserEntity user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> {
                    log.warn("Login failed: user not found, email={}", maskEmail(request.email()));
                    return new InvalidCredentialsException();
                });

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            log.warn("Login failed: invalid password, userId={}", user.getId());
            throw new InvalidCredentialsException();
        }

        JwtTokenPairResponseDto tokens = sessionService.newSession(user.getId());
        log.info("Login successful, userId={}", user.getId());

        return tokens;
    }

    @Transactional
    public void changePassword(ChangePasswordRequestDto request, UUID userId) {
        log.debug("Password change attempt, userId={}", userId);

        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.error("Password change failed: user not found, userId={}", userId);
                    return new UserNotFoundException();
                });

        if (!passwordEncoder.matches(request.oldPassword(), user.getPasswordHash())) {
            log.warn("Password change failed: invalid current password, userId={}", userId);
            throw new InvalidPasswordException();
        }

        if (passwordEncoder.matches(request.newPassword(), user.getPasswordHash())) {
            log.warn("Password change failed: new password equals current, userId={}", userId);
            throw new SamePasswordException();
        }

        user.setPasswordHash(passwordEncoder.encode(request.newPassword()));
        log.info("Password changed, userId={}", userId);

        sessionService.terminateAllSessions(userId);
        log.info("All sessions terminated after password change, userId={}", userId);
    }

    private static String maskEmail(String email) {
        if (email == null || !email.contains("@")) return "***";
        int at = email.indexOf('@');
        if (at <= 1) return "***" + email.substring(at);
        return email.charAt(0) + "***" + email.substring(at);
    }
}