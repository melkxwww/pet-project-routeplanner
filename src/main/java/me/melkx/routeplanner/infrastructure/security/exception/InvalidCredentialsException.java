package me.melkx.routeplanner.infrastructure.security.exception;

public class InvalidCredentialsException extends AuthException {
    public InvalidCredentialsException() {
        super(401, "Invalid email or password");
    }
}