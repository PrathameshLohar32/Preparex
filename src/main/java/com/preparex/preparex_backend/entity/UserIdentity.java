package com.preparex.preparex_backend.entity;

import com.preparex.preparex_backend.common.BaseEntity;
import com.preparex.preparex_backend.enums.AuthProvider;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

/**
 * Tracks which OAuth providers a user has linked.
 * A user can have multiple identities (e.g., Google + Local).
 */
@Entity
@Table(
        name = "user_identities",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uq_provider_provider_user_id",
                        columnNames = {"provider", "provider_user_id"}
                )
        },
        indexes = {
                @Index(name = "idx_user_identities_user_id", columnList = "user_id"),
                @Index(name = "idx_user_identities_provider", columnList = "provider, provider_user_id")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserIdentity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "provider", nullable = false)
    private AuthProvider provider;

    /**
     * The unique identifier from the external provider (e.g. Google's "sub" claim).
     * Null for LOCAL provider.
     */
    @Column(name = "provider_user_id")
    private String providerUserId;
}
