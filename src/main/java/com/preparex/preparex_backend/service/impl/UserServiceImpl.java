package com.preparex.preparex_backend.service.impl;

import com.preparex.preparex_backend.entity.User;
import com.preparex.preparex_backend.exception.DuplicateResourceException;
import com.preparex.preparex_backend.exception.UserNotFoundException;
import com.preparex.preparex_backend.repository.UserRepository;
import com.preparex.preparex_backend.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Optional;
import java.util.UUID;

/**
 * Handles user entity operations: lookup, creation, uniqueness validation.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<User> findByPhone(String phone) {
        return userRepository.findByPhone(phone);
    }

    @Override
    @Transactional(readOnly = true)
    public User findByIdOrThrow(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId.toString()));
    }

    /**
     * Validates uniqueness of email, phone, and username before registration.
     * Checks each field only if it is non-blank to avoid false conflicts.
     */
    @Override
    @Transactional(readOnly = true)
    public void validateUniqueness(String email, String phone, String username) {
        if (StringUtils.hasText(email) && userRepository.existsByEmail(email)) {
            throw new DuplicateResourceException("Email", email);
        }
        if (StringUtils.hasText(phone) && userRepository.existsByPhone(phone)) {
            throw new DuplicateResourceException("Phone", phone);
        }
        if (StringUtils.hasText(username) && userRepository.existsByUsername(username)) {
            throw new DuplicateResourceException("Username", username);
        }
    }

    @Override
    @Transactional
    public User createUser(String name, String username, String email, String phone, String passwordHash) {
        User user = User.builder()
                .name(name)
                .username(username)
                .email(email)
                .phone(phone)
                .passwordHash(passwordHash)
                .isEmailVerified(StringUtils.hasText(email))
                .isPhoneVerified(StringUtils.hasText(phone))
                .isActive(true)
                .build();
        User saved = userRepository.save(user);
        log.info("Created new user userId={} username={}", saved.getId(), username);
        return saved;
    }
}
