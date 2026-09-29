package me.melkx.routeplanner.infrastructure.security;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import me.melkx.common.lang.ValueResult;
import me.melkx.common.security.jwt.AuthorizationHeaderExtractingUtil;
import me.melkx.routeplanner.infrastructure.security.dto.*;
import me.melkx.routeplanner.infrastructure.security.exception.InvalidRefreshTokenException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/auth")
public class JwtAuthController {
    private final JwtAuthService authService;
    private final JwtSessionService sessionService;

    @Autowired
    public JwtAuthController(JwtAuthService authService, JwtSessionService sessionService) {
        this.authService = authService;
        this.sessionService = sessionService;
    }

    @PostMapping("/register")
    public ResponseEntity<JwtTokenPairResponseDto> register(@Valid @RequestBody RegisterRequestDto request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.register(request));
    }

    @PostMapping("/login")
    public ResponseEntity<JwtTokenPairResponseDto> login(@Valid @RequestBody LoginRequestDto request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/change-password")
    public ResponseEntity<Void> changePassword(@Valid @RequestBody ChangePasswordRequestDto request,
                                               @AuthenticationPrincipal CustomUserDetails userDetails) {
        authService.changePassword(request, userDetails.id());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/refresh")
    public ResponseEntity<JwtTokenPairResponseDto> refresh(
            @RequestHeader(value = "Authorization", required = false) String header) {
        ValueResult<String> result = AuthorizationHeaderExtractingUtil.extract(header);
        if (!result.valid()) {
            log.debug("Refresh failed: invalid or missing Authorization header");
            throw new InvalidRefreshTokenException("Invalid refresh token provided");
        }
        return ResponseEntity.ok(sessionService.extendSession(result.getOrThrow()));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@Valid @RequestBody TerminateRequestDto request,
                                       @AuthenticationPrincipal CustomUserDetails userDetails) {
        sessionService.terminateSession(request, userDetails.id());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/logout-all")
    public ResponseEntity<Void> logoutAll(@AuthenticationPrincipal CustomUserDetails userDetails) {
        sessionService.terminateAllSessions(userDetails.id());
        return ResponseEntity.noContent().build();
    }
}