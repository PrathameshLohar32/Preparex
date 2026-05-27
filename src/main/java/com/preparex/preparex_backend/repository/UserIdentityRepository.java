package com.preparex.preparex_backend.repository;

import com.preparex.preparex_backend.entity.UserIdentity;
import com.preparex.preparex_backend.entity.User;
import com.preparex.preparex_backend.enums.AuthProvider;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserIdentityRepository extends JpaRepository<UserIdentity, UUID> {

    Optional<UserIdentity> findByProviderAndProviderUserId(AuthProvider provider, String providerUserId);

    List<UserIdentity> findByUser(User user);
}
