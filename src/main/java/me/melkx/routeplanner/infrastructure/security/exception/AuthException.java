package me.melkx.routeplanner.infrastructure.security.exception;

public abstract class AuthException extends RuntimeException {

    private final int status;

    protected AuthException(int status, String message) {
        super(message);
        this.status = status;
    }

    public int getStatus() {
        return status;
    }
}
