package me.melkx.routeplanner.module.user;

public record UserPersonalInfoResponseDto(
        Long id,
        String email,
        Boolean activated) {
}
