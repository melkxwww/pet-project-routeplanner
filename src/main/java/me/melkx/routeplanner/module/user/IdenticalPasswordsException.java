package me.melkx.routeplanner.module.user;

public class IdenticalPasswordsException extends RuntimeException {
    public IdenticalPasswordsException(String message) {
        super(message);
    }
}
