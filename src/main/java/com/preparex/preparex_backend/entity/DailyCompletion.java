package com.preparex.preparex_backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Daily completion entity — tracks which daily problems a user has completed.
 * Unique constraint on (user_id, daily_problem_id) ensures idempotent completion.
 */
@Entity
@Table(
        name = "daily_completions",
        uniqueConstraints = @UniqueConstraint(
                name = "uq_dc_user_daily_problem",
                columnNames = {"user_id", "daily_problem_id"}
        ),
        indexes = {
                @Index(name = "idx_dc_user_completed_date", columnList = "user_id, completed_date")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DailyCompletion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", updatable = false, nullable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "daily_problem_id", nullable = false)
    private DailyProblem dailyProblem;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "submission_id")
    private Submission submission;

    @Column(name = "completed_date", nullable = false)
    private LocalDate completedDate;

    @Column(name = "completed_at")
    @Builder.Default
    private Instant completedAt = Instant.now();
}
