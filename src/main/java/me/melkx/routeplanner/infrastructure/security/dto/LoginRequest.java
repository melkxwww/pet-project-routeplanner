package me.melkx.routeplanner.infrastructure.security.dto;

import jakarta.validation.constraints.Email;
import me.melkx.routeplanner.infrastructure.security.Password;

public record LoginRequest(@Email String email, @Password String password) {
}
