package com.redhope.service;

import com.redhope.dto.request.SignupRequest;
import com.redhope.entity.User;
import com.redhope.enums.UserStatus;
import com.redhope.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public User registerUser(SignupRequest signupRequest) {
        if (userRepository.existsByEmail(signupRequest.getEmail())) {
            throw new IllegalArgumentException("Email is already registered");
        }

        User user = new User();
        user.setFullName(signupRequest.getFullName());
        user.setEmail(signupRequest.getEmail());
        user.setPassword(passwordEncoder.encode(signupRequest.getPassword()));
        user.setPhone(signupRequest.getPhone());
        user.setCity(signupRequest.getCity());
        user.setStatus(UserStatus.ACTIVE);
        user.setEmailVerified(false);
        user.setRole(com.redhope.enums.Role.ROLE_USER);

        if (signupRequest.getBloodType() != null && !signupRequest.getBloodType().isEmpty()) {
            user.setBloodType(com.redhope.enums.BloodType.valueOf(signupRequest.getBloodType()));
        }

        return userRepository.save(user);
    }
}
