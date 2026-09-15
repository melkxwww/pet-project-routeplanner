package me.melkx.veloroute.infrastructure.security.service;

import me.melkx.common.jwt.model.RefreshTokenPayload;

import java.util.UUID;

public record UserRefreshTokenPayload(long sub, UUID jti) implements RefreshTokenPayload {
}
