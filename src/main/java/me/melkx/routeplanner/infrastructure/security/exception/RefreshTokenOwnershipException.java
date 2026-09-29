package me.melkx.routeplanner.infrastructure.security.exception;

public class RefreshTokenOwnershipException extends AuthException {
    public RefreshTokenOwnershipException() {
        super(403, "Refresh token does not belong to this user");
    }
}
