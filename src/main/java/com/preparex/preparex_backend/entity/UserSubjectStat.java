package com.preparex.preparex_backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

/**
 * User subject stat entity — per-subject accuracy for radar chart.
 * UNIQUE(user_id, subject_id) — one row per user per subject.
 * Updated asynchronously by ProfileStatsConsumer via Kafka.
 */
@Entity
@Table(
        name = "user_subject_stats",
        uniqueConstraints = @UniqueConstraint(
                name = "uq_user_subject_stats",
                columnNames = {"user_id", "subject_id"}
        ),
        indexes = @Index(name = "idx_user_subject_stats_user", columnList = "user_id")
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserSubjectStat {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subject_id", nullable = false)
    private Subject subject;

    @Column(name = "solved", nullable = false)
    @Builder.Default
    private Integer solved = 0;

    @Column(name = "attempted", nullable = false)
    @Builder.Default
    private Integer attempted = 0;

    @Column(name = "accuracy", nullable = false)
    @Builder.Default
    private Double accuracy = 0.0;

    @Column(name = "updated_at")
    @Builder.Default
    private Instant updatedAt = Instant.now();
}
