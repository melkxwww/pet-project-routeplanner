package me.melkx.veloroute.module.user.service;

import me.melkx.veloroute.module.user.db.entity.UserEntity;
import me.melkx.veloroute.module.user.db.repository.UserRepository;
import me.melkx.veloroute.module.user.dto.request.UserPasswordChangingRequestDto;
import me.melkx.veloroute.module.user.dto.response.UserPersonalInfoResponseDto;
import me.melkx.veloroute.module.user.exception.IdenticalPasswordsException;
import me.melkx.veloroute.module.user.exception.InvalidPasswordException;
import me.melkx.veloroute.module.user.exception.UserNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public UserPersonalInfoResponseDto getPersonalInfo(long userId) {
        UserEntity user = getUserById(userId);

        return new UserPersonalInfoResponseDto(
                user.getId(),
                user.getEmail(),
                user.getActivated()
        );
    }

    @Transactional
    public void changePassword(UserPasswordChangingRequestDto request) {
        UserEntity user = getUserById(request.userId());

        if(!passwordEncoder.matches(request.oldPassword(), user.getPasswordHash()))
            throw new InvalidPasswordException("Invalid password");

        if(passwordEncoder.matches(request.newPassword(), user.getPasswordHash()))
            throw new IdenticalPasswordsException("Identical passwords");

        user.setPasswordHash(passwordEncoder.encode(request.newPassword()));
    }

    private UserEntity getUserById(long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));
    }
}
