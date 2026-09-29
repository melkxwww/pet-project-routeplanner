package me.melkx.routeplanner.infrastructure.security.exception;

public class SamePasswordException extends AuthException {
    public SamePasswordException() {
        super(400, "New password must differ from current");
    }
}
