package me.melkx.routeplanner.infrastructure.security;

import me.melkx.routeplanner.infrastructure.security.dto.JwtTokenPair;
import me.melkx.routeplanner.infrastructure.security.dto.JwtTokenPairResponse;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface JwtTokenPairMapper {
    JwtTokenPairResponse map(JwtTokenPair obj);
}
