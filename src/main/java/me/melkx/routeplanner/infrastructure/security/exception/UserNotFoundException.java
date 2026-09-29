package me.melkx.routeplanner.infrastructure.security.exception;

public class UserNotFoundException extends AuthException {
    public UserNotFoundException() {
        super(404, "User not found");
    }
}
