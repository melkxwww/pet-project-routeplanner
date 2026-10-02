package me.melkx.routeplanner.infrastructure.security;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

@ConfigurationProperties(prefix = "routeplanner.jwt")
public record JwtValidityProperties(@DefaultValue("300") int accessTokenValiditySeconds,
                                    @DefaultValue("3600") int refreshTokenValiditySeconds) {
}
