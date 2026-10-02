package me.melkx.routeplanner.infrastructure.security;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import me.melkx.common.security.jwt.util.AuthorizationHeaderExtractor;
import me.melkx.routeplanner.infrastructure.security.dto.*;
import me.melkx.routeplanner.infrastructure.security.service.AccountService;
import me.melkx.routeplanner.infrastructure.security.service.SessionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {
    private final AccountService authService;
    private final SessionService sessionService;
    private final JwtTokenPairMapper tokenPairMapper;

    @Autowired
    public AuthController(AccountService authService, SessionService sessionService, JwtTokenPairMapper tokenPairMapper) {
        this.authService = authService;
        this.sessionService = sessionService;
        this.tokenPairMapper = tokenPairMapper;
    }

    @PostMapping("/register")
    public ResponseEntity<JwtTokenPairResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(tokenPairMapper.map(
                authService.register(request.email(), request.password())
        ));
    }

    @PostMapping("/login")
    public ResponseEntity<JwtTokenPairResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(tokenPairMapper.map(
                authService.login(request.email(), request.password())
        ));
    }

    @PostMapping("/refresh")
    public ResponseEntity<JwtTokenPairResponse> refresh(
            @RequestHeader("Authorization") String header) {
        return ResponseEntity.ok(tokenPairMapper.map(
                sessionService.extendSession(AuthorizationHeaderExtractor.extract(header))
        ));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@Valid @RequestBody LogoutRequest request,
                                       @AuthenticationPrincipal UserContext user) {
        sessionService.terminateSession(request.refreshToken(), user.id());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/logout-all")
    public ResponseEntity<Void> logoutAll(@AuthenticationPrincipal UserContext user) {
        sessionService.terminateAllSessions(user.id());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/change-password")
    public ResponseEntity<Void> changePassword(@Valid @RequestBody ChangePasswordRequest request,
                                               @AuthenticationPrincipal UserContext user) {
        authService.changePassword(new ChangePasswordCommand(
                request.oldPassword(), request.newPassword(), user.id()
        ));
        return ResponseEntity.noContent().build();
    }
}