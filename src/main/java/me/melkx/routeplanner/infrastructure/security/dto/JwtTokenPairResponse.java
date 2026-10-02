package me.melkx.routeplanner.infrastructure.security.dto;

public record JwtTokenPairResponse(String accessToken, String refreshToken) {
}
