package me.melkx.routeplanner.infrastructure.security;

import me.melkx.common.security.jwt.JwtUserDetailsLoadingException;
import me.melkx.common.security.jwt.JwtUserDetailsService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class CustomUserDetailsLoader implements JwtUserDetailsService<CustomUserDetailsLoader.ToParseAccessTokenPayload> {
    @Override
    public UserDetails loadByPayload(ToParseAccessTokenPayload payload) throws JwtUserDetailsLoadingException {
        return new CustomUserDetails(payload.sub());
    }

    @Override
    public Class<ToParseAccessTokenPayload> payloadType() {
        return ToParseAccessTokenPayload.class;
    }

    public record ToParseAccessTokenPayload(UUID sub) {
    }
}
