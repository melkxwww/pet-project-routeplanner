package me.melkx.routeplanner.module.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;

public record UserRegistrationRequestDto(
        @NotNull
        @Email
        String email,
        @NotNull
        @Password
        String password) {
}
