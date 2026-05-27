package com.preparex.preparex_backend.service;

import com.preparex.preparex_backend.entity.User;

import java.util.Optional;
import java.util.UUID;

public interface UserService {

    Optional<User> findByEmail(String email);

    Optional<User> findByPhone(String phone);

    User findByIdOrThrow(UUID userId);

    void validateUniqueness(String email, String phone, String username);

    User createUser(String name, String username, String email, String phone, String passwordHash);
}
