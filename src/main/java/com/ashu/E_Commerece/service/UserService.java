package com.ashu.E_Commerece.service;

import com.ashu.E_Commerece.dto.user.PasswordChangeRequest;
import com.ashu.E_Commerece.dto.user.UserResponse;
import com.ashu.E_Commerece.dto.user.UserUpdateRequest;
import com.ashu.E_Commerece.exception.BadRequestException;
import com.ashu.E_Commerece.exception.ResourceNotFoundException;
import com.ashu.E_Commerece.model.User;
import com.ashu.E_Commerece.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service for user management operations.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Get current authenticated user.
     */
    public User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));
    }

    /**
     * Get user profile.
     */
    @Transactional(readOnly = true)
    public UserResponse getProfile() {
        User user = getCurrentUser();
        return mapToResponse(user);
    }

    /**
     * Get user by ID.
     */
    @Transactional(readOnly = true)
    public UserResponse getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));
        return mapToResponse(user);
    }

    /**
     * Update user profile.
     */
    @Transactional
    public UserResponse updateProfile(UserUpdateRequest request) {
        User user = getCurrentUser();

        if (request.getFirstName() != null) {
            user.setFirstName(request.getFirstName());
        }
        if (request.getLastName() != null) {
            user.setLastName(request.getLastName());
        }
        if (request.getPhone() != null) {
            user.setPhone(request.getPhone());
        }
        if (request.getAddress() != null) {
            user.setAddress(request.getAddress());
        }

        user = userRepository.save(user);
        log.info("User profile updated: {}", user.getEmail());

        return mapToResponse(user);
    }

    /**
     * Update profile image URL.
     */
    @Transactional
    public UserResponse updateProfileImage(String imageUrl) {
        User user = getCurrentUser();
        user.setProfileImageUrl(imageUrl);
        user = userRepository.save(user);
        return mapToResponse(user);
    }

    /**
     * Change password.
     */
    @Transactional
    public void changePassword(PasswordChangeRequest request) {
        User user = getCurrentUser();

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new BadRequestException("Current password is incorrect");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
        log.info("Password changed for user: {}", user.getEmail());
    }

    /**
     * Count total users.
     */
    @Transactional(readOnly = true)
    public long countUsers() {
        return userRepository.count();
    }

    private UserResponse mapToResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .phone(user.getPhone())
                .role(user.getRole().name())
                .address(user.getAddress())
                .profileImageUrl(user.getProfileImageUrl())
                .createdAt(user.getCreatedAt())
                .build();
    }
}
