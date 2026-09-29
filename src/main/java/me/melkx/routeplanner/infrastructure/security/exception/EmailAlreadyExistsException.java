package me.melkx.routeplanner.infrastructure.security.exception;

public class EmailAlreadyExistsException extends AuthException {
    public EmailAlreadyExistsException() {
        super(409, "Email already exists");
    }
}
