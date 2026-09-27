package me.melkx.routeplanner.module.user;

import jakarta.validation.constraints.NotNull;

public record UserPasswordChangingRequestDto(
        @NotNull Long userId,
        @NotNull @Password String oldPassword,
        @NotNull @Password String newPassword) {
}
