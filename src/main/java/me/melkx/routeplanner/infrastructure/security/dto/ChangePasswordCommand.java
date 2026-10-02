package me.melkx.routeplanner.infrastructure.security.dto;

import java.util.UUID;

public record ChangePasswordCommand(String oldPassword, String newPassword, UUID userId) {
}
