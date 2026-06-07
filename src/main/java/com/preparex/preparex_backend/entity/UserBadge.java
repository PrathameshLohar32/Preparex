package com.preparex.preparex_backend.entity;

import com.preparex.preparex_backend.enums.BadgeType;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

/**
 * User badge entity — tracks earned badges.
 * UNIQUE(user_id, badge_type) prevents duplicate awards.
 * Awarded by BadgeService, consumed by BadgeConsumer via Kafka.
 */
@Entity
@Table(
        name = "user_badges",
        uniqueConstraints = @UniqueConstraint(
                name = "uq_user_badges",
                columnNames = {"user_id", "badge_type"}
        ),
        indexes = @Index(name = "idx_user_badges_user", columnList = "user_id")
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserBadge {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "badge_type", nullable = false, length = 50)
    private BadgeType badgeType;

    @Column(name = "context", length = 255)
    private String context;

    @Column(name = "awarded_at", nullable = false)
    @Builder.Default
    private Instant awardedAt = Instant.now();
}
