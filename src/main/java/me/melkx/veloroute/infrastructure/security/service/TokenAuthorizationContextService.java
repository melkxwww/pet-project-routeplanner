package me.melkx.veloroute.infrastructure.security.service;

import me.melkx.common.auth.exception.AuthenticationContextLoadingException;
import me.melkx.common.auth.jwt.service.JwtAuthenticationContextService;
import me.melkx.common.auth.model.AuthenticationContext;
import me.melkx.common.jwt.service.JwtParser;
import me.melkx.common.utils.ValueResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
public class TokenAuthorizationContextService implements JwtAuthenticationContextService {
    private final JwtParser jwtParser;

    @Autowired
    public TokenAuthorizationContextService(JwtParser jwtParser) {
        this.jwtParser = jwtParser;
    }

    @Override
    public AuthenticationContext loadByToken(String token) throws AuthenticationContextLoadingException {
        ValueResult<UserAccessTokenPayload> parseResult = jwtParser.parse(token, UserAccessTokenPayload.class);
        if (!parseResult.valid())
            throw new AuthenticationContextLoadingException(parseResult.errorMessage());

        UserInfo userInfo = new UserInfo(parseResult.getOrThrow().sub());
        return new AuthenticationContext(
                userInfo,
                Set.of()
        );
    }
}
