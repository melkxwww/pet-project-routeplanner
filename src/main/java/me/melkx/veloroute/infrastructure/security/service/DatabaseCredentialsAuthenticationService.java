package me.melkx.veloroute.infrastructure.security.service;

import me.melkx.common.auth.authentication.credentials.dto.request.CredentialsLoginRequestDto;
import me.melkx.common.auth.authentication.credentials.dto.request.CredentialsRegistrationRequestDto;
import me.melkx.common.auth.authentication.credentials.dto.response.JwtTokenPairResponseDto;
import me.melkx.common.auth.authentication.credentials.exception.ExtendSessionProcessingException;
import me.melkx.common.auth.authentication.credentials.exception.LoginProcessingException;
import me.melkx.common.auth.authentication.credentials.exception.LogoutProcessingException;
import me.melkx.common.auth.authentication.credentials.exception.RegisterProcessingException;
import me.melkx.common.auth.authentication.credentials.service.CredentialsAuthenticationService;
import org.springframework.stereotype.Service;

@Service
public class DatabaseCredentialsAuthenticationService implements CredentialsAuthenticationService {
    @Override
    public JwtTokenPairResponseDto login(CredentialsLoginRequestDto credentialsLoginRequestDto) throws LoginProcessingException {
        return null;
    }

    @Override
    public JwtTokenPairResponseDto register(CredentialsRegistrationRequestDto credentialsRegistrationRequestDto) throws RegisterProcessingException {
        return null;
    }

    @Override
    public JwtTokenPairResponseDto extendSession(String refreshToken) throws ExtendSessionProcessingException {
        return null;
    }

    @Override
    public void logout(String accessToken, String refreshToken, boolean b) throws LogoutProcessingException {

    }
}
