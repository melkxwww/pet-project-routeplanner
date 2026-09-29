package me.melkx.routeplanner.infrastructure.security;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.stereotype.Component;

@ConfigurationProperties(prefix = "routeplanner.security.jwt")
@Component
public record JwtValidityProperties(@DefaultValue("300") int accessTokenValiditySeconds,
                                    @DefaultValue("3600") int refreshTokenValiditySeconds) {
}
