package com.preparex.preparex_backend.entity;

import com.preparex.preparex_backend.common.BaseEntity;
import com.preparex.preparex_backend.enums.SprintSessionStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

/**
 * Sprint session entity — tracks a 30-minute timed blitz session.
 * Live session state (question queue, skips remaining) is maintained in Redis;
 * this entity persists the finalized metadata and scoring summary.
 */
@Entity
@Table(
        name = "sprint_sessions",
        indexes = {
                @Index(name = "idx_sprint_sessions_user_status", columnList = "user_id, status"),
                @Index(name = "idx_sprint_sessions_started_at", columnList = "started_at")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SprintSession extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private SprintSessionStatus status = SprintSessionStatus.ACTIVE;

    @Column(name = "subject_filter", length = 50)
    private String subjectFilter;

    @Column(name = "difficulty_filter", length = 10)
    private String difficultyFilter;

    @Column(name = "total_questions_attempted", nullable = false)
    @Builder.Default
    private Integer totalQuestionsAttempted = 0;

    @Column(name = "total_correct", nullable = false)
    @Builder.Default
    private Integer totalCorrect = 0;

    @Column(name = "total_wrong", nullable = false)
    @Builder.Default
    private Integer totalWrong = 0;

    @Column(name = "total_skipped", nullable = false)
    @Builder.Default
    private Integer totalSkipped = 0;

    @Column(name = "sprint_points", nullable = false)
    @Builder.Default
    private Integer sprintPoints = 0;

    @Column(name = "started_at", nullable = false)
    @Builder.Default
    private Instant startedAt = Instant.now();

    @Column(name = "ended_at")
    private Instant endedAt;

    @Column(name = "exam_id", length = 50)
    private String examId;
}
