package me.melkx.routeplanner.infrastructure.security.exception;

public class InvalidRefreshTokenException extends AuthException {
    public InvalidRefreshTokenException(String reason) {
        super(401, reason);
    }
}