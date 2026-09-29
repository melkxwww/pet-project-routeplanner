package me.melkx.routeplanner.infrastructure.security.dto;

import me.melkx.routeplanner.infrastructure.security.Password;

public record ChangePasswordRequestDto(@Password String oldPassword, @Password String newPassword) {
}
