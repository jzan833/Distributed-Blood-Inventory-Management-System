package com.redhope.service;

import com.redhope.entity.User;
import com.redhope.enums.BloodType;
import com.redhope.enums.Role;
import com.redhope.enums.UserStatus;
import com.redhope.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class UserService {

    private static final String CHARACTERS = "ABCDEFGHJKMNPQRSTUVWXYZ23456789";
    private static final SecureRandom RANDOM = new SecureRandom();

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public Optional<User> getUserById(Long id) {
        return userRepository.findById(id);
    }

    public List<User> searchUsers(String name, String email, BloodType bloodType, UserStatus status) {
        List<User> users = userRepository.findAll();

        if (name != null && !name.trim().isEmpty()) {
            String lowerName = name.trim().toLowerCase();
            users = users.stream()
                    .filter(u -> u.getFullName() != null && u.getFullName().toLowerCase().contains(lowerName))
                    .collect(Collectors.toList());
        }

        if (email != null && !email.trim().isEmpty()) {
            String lowerEmail = email.trim().toLowerCase();
            users = users.stream()
                    .filter(u -> u.getEmail() != null && u.getEmail().toLowerCase().contains(lowerEmail))
                    .collect(Collectors.toList());
        }

        if (bloodType != null) {
            users = users.stream()
                    .filter(u -> u.getBloodType() == bloodType)
                    .collect(Collectors.toList());
        }

        if (status != null) {
            users = users.stream()
                    .filter(u -> u.getStatus() == status)
                    .collect(Collectors.toList());
        }

        return users;
    }

    public User banUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + userId));
        user.setStatus(UserStatus.BANNED);
        return userRepository.save(user);
    }

    public User unbanUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + userId));
        user.setStatus(UserStatus.ACTIVE);
        return userRepository.save(user);
    }

    public User changeUserRole(Long userId, Role newRole) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + userId));
        user.setRole(newRole);
        return userRepository.save(user);
    }

    public String resetUserPassword(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + userId));
        String newPassword = generateRandomPassword(10);
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        return newPassword;
    }

    private String generateRandomPassword(int length) {
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(CHARACTERS.charAt(RANDOM.nextInt(CHARACTERS.length())));
        }
        return sb.toString();
    }
}
