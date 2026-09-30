package me.melkx.routeplanner.infrastructure.security.service;

import me.melkx.common.security.jwt.JwtUserDetailsLoadingException;
import me.melkx.common.security.jwt.JwtUserDetailsService;
import me.melkx.routeplanner.infrastructure.security.UserContext;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class JwtUserContextService implements JwtUserDetailsService<JwtUserContextService.ToParseAccessTokenPayload> {
    @Override
    public UserDetails loadByPayload(ToParseAccessTokenPayload payload) throws JwtUserDetailsLoadingException {
        return new UserContext(payload.sub());
    }

    @Override
    public Class<ToParseAccessTokenPayload> payloadType() {
        return ToParseAccessTokenPayload.class;
    }

    public record ToParseAccessTokenPayload(UUID sub) {
    }
}
