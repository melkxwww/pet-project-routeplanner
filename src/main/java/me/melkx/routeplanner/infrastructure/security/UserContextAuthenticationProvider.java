package me.melkx.routeplanner.infrastructure.security;

import me.melkx.common.jwt.JwtParser;
import me.melkx.common.security.jwt.JwtAuthenticationProvider;
import me.melkx.common.security.jwt.JwtAuthenticationToken;
import org.jspecify.annotations.Nullable;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.UUID;

@Component
public class UserContextAuthenticationProvider implements JwtAuthenticationProvider {
    private final JwtParser jwtParser;

    public UserContextAuthenticationProvider(JwtParser jwtParser) {
        this.jwtParser = jwtParser;
    }

    @Override
    public @Nullable Authentication authenticate(Authentication authentication) throws AuthenticationException {
        if (!(authentication instanceof JwtAuthenticationToken))
            throw new AuthenticationServiceException("Invalid authentication provided!");

        String token = (String) authentication.getPrincipal();
        if (token == null)
            throw new AuthenticationServiceException("token cannot be null");

        ToParseAccessTokenPayload payload = jwtParser.parse(token, ToParseAccessTokenPayload.class);

        return new JwtAuthenticationToken(
                new UserContext(payload.sub()), Set.of()
        );
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return UserContextAuthenticationProvider.class.isAssignableFrom(authentication);
    }

    record ToParseAccessTokenPayload(UUID sub) {
    }
}
