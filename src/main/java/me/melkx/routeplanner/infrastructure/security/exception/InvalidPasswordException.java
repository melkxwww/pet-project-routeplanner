package me.melkx.routeplanner.infrastructure.security.exception;

public class InvalidPasswordException extends AuthException {
    public InvalidPasswordException() {
        super(400, "Current password is incorrect");
    }
}
