package me.melkx.routeplanner.infrastructure.security.dto;

public record JwtTokenPair(String accessToken, String refreshToken) {
}
