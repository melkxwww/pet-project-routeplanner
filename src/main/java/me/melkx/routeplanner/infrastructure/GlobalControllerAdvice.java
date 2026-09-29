package me.melkx.routeplanner.infrastructure;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import me.melkx.common.web.FormattedError;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalControllerAdvice {
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<FormattedError> handleValidation(MethodArgumentNotValidException ex,
                                                           HttpServletRequest request) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(e -> e.getField() + ": " + e.getDefaultMessage())
                .collect(Collectors.joining(", "));

        log.debug("Validation failed at {}: {}", request.getRequestURI(), message);
        return build(400, message, request);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<FormattedError> handleNotReadable(HttpMessageNotReadableException ex,
                                                            HttpServletRequest request) {
        log.debug("Malformed request body at {}: {}", request.getRequestURI(), ex.getMessage());
        return build(400, "Malformed request body", request);
    }

    @ExceptionHandler(MissingRequestHeaderException.class)
    public ResponseEntity<FormattedError> handleMissingHeader(MissingRequestHeaderException ex,
                                                              HttpServletRequest request) {
        log.debug("Missing header at {}: {}", request.getRequestURI(), ex.getHeaderName());
        return build(400, "Missing header: " + ex.getHeaderName(), request);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<FormattedError> handleTypeMismatch(MethodArgumentTypeMismatchException ex,
                                                             HttpServletRequest request) {
        String message = "Invalid value for parameter '" + ex.getName() + "'";
        log.debug("Type mismatch at {}: {}", request.getRequestURI(), message);
        return build(400, message, request);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<FormattedError> handleMethodNotSupported(HttpRequestMethodNotSupportedException ex,
                                                                   HttpServletRequest request) {
        log.debug("Method not supported at {}: {}", request.getRequestURI(), ex.getMethod());
        return build(405, "Method not supported: " + ex.getMethod(), request);
    }

    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<FormattedError> handleNoHandler(NoHandlerFoundException ex,
                                                          HttpServletRequest request) {
        log.debug("No handler found at {}: {}", request.getRequestURI(), ex.getRequestURL());
        return build(404, "Endpoint not found", request);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<FormattedError> handleGeneric(Exception ex, HttpServletRequest request) {
        log.error("Unhandled exception at {}", request.getRequestURI(), ex);
        return build(500, "Internal server error", request);
    }

    private ResponseEntity<FormattedError> build(int status, String message, HttpServletRequest request) {
        FormattedError error = new FormattedError(
                HttpStatus.valueOf(status).getReasonPhrase(),
                status,
                message,
                request.getRequestURI(),
                LocalDateTime.now()
        );
        return ResponseEntity.status(status).body(error);
    }
}
