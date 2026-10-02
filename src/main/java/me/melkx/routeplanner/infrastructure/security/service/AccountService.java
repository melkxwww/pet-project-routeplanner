package me.melkx.routeplanner.infrastructure.security.service;

import lombok.extern.slf4j.Slf4j;
import me.melkx.routeplanner.infrastructure.security.dto.*;
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
public class AccountService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final SessionService sessionService;

    @Autowired
    public AccountService(UserRepository userRepository,
                          PasswordEncoder passwordEncoder,
                          SessionService sessionService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.sessionService = sessionService;
    }

    @Transactional
    public JwtTokenPair register(String email, String password) {
        log.debug("Registration attempt, email={}", maskEmail(email));

        if (userRepository.existsEmail(email)) {
            log.warn("Registration failed: email already exists, email={}", maskEmail(email));
            throw new EmailAlreadyExistsException();
        }

        UserEntity user = UserEntity.builder()
                .email(email)
                .passwordHash(passwordEncoder.encode(password))
                .build();

        userRepository.save(user);
        log.info("User registered, userId={}", user.getId());

        JwtTokenPair tokens = sessionService.newSession(user.getId());
        return tokens;
    }

    public JwtTokenPair login(String email, String password) {
        log.debug("Login attempt, email={}", maskEmail(email));

        UserEntity user = userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    log.warn("Login failed: user not found, email={}", maskEmail(email));
                    return new InvalidCredentialsException();
                });

        if (!passwordEncoder.matches(password, user.getPasswordHash())) {
            log.warn("Login failed: invalid password, userId={}", user.getId());
            throw new InvalidCredentialsException();
        }

        JwtTokenPair tokens = sessionService.newSession(user.getId());
        log.info("Login successful, userId={}", user.getId());

        return tokens;
    }

    @Transactional
    public void changePassword(ChangePasswordCommand command) {
        log.debug("Password change attempt, userId={}", command.userId());

        UserEntity user = userRepository.findById(command.userId())
                .orElseThrow(() -> {
                    log.error("Password change failed: user not found, userId={}", command.userId());
                    return new UserNotFoundException();
                });

        if (!passwordEncoder.matches(command.oldPassword(), user.getPasswordHash())) {
            log.warn("Password change failed: invalid current password, userId={}", command.userId());
            throw new InvalidPasswordException();
        }

        if (passwordEncoder.matches(command.newPassword(), user.getPasswordHash())) {
            log.warn("Password change failed: new password equals current, userId={}", command.userId());
            throw new SamePasswordException();
        }

        user.setPasswordHash(passwordEncoder.encode(command.newPassword()));
        log.info("Password changed, userId={}", command.userId());

        sessionService.terminateAllSessions(command.userId());
        log.info("All sessions terminated after password change, userId={}", command.userId());
    }

    private static String maskEmail(String email) {
        if (email == null || !email.contains("@")) return "***";
        int at = email.indexOf('@');
        if (at <= 1) return "***" + email.substring(at);
        return email.charAt(0) + "***" + email.substring(at);
    }
}