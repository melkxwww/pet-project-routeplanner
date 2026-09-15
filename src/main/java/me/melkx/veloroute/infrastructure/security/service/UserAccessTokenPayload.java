package me.melkx.veloroute.infrastructure.security.service;

import me.melkx.common.jwt.model.AccessTokenPayload;

public record UserAccessTokenPayload(long sub) implements AccessTokenPayload {
}
