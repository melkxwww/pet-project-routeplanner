package me.melkx.routeplanner.infrastructure.security.dto;

public record JwtTokenPairResponseDto(String accessToken, String refreshToken) {
}
