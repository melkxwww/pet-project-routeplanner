package me.melkx.routeplanner.infrastructure.security;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import me.melkx.common.web.FormattedError;
import me.melkx.routeplanner.infrastructure.security.exception.AuthException;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;

@Slf4j
@Order(Ordered.HIGHEST_PRECEDENCE)
@RestControllerAdvice(basePackages = "me.melkx.routeplanner.infrastructure.security")
public class AuthControllerAdvice {

    @ExceptionHandler(AuthException.class)
    public ResponseEntity<FormattedError> handleAuthException(AuthException ex,
                                                              HttpServletRequest request) {
        FormattedError error = new FormattedError(
                HttpStatus.valueOf(ex.getStatus()).getReasonPhrase(),
                ex.getStatus(),
                ex.getMessage(),
                request.getRequestURI(),
                LocalDateTime.now()
        );
        return ResponseEntity.status(ex.getStatus()).body(error);
    }
}