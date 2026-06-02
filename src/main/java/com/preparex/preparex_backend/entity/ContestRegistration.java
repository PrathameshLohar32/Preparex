package com.preparex.preparex_backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

/**
 * Contest registration entity — tracks user enrollment and attempt status.
 * Unique constraint on (contest_id, user_id) ensures idempotent registration.
 */
@Entity
@Table(
        name = "contest_registrations",
        uniqueConstraints = @UniqueConstraint(
                name = "uq_cr_contest_user",
                columnNames = {"contest_id", "user_id"}
        )
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ContestRegistration {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", updatable = false, nullable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contest_id", nullable = false)
    private Contest contest;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "started")
    @Builder.Default
    private Boolean started = false;

    @Column(name = "started_at")
    private Instant startedAt;

    @Column(name = "final_submitted_at")
    private Instant finalSubmittedAt;

    @Column(name = "registered_at")
    @Builder.Default
    private Instant registeredAt = Instant.now();
}
