package com.preparex.preparex_backend.entity;

import com.preparex.preparex_backend.enums.SprintAnswerStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

/**
 * Sprint answer entity — records a single question attempt within a sprint session.
 * Each row captures whether the user answered correctly, incorrectly, or skipped.
 */
@Entity
@Table(
        name = "sprint_answers",
        indexes = {
                @Index(name = "idx_sprint_answers_session", columnList = "session_id"),
                @Index(name = "idx_sprint_answers_session_problem", columnList = "session_id, problem_id")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SprintAnswer {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id", nullable = false)
    private SprintSession session;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "problem_id", nullable = false)
    private Problem problem;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private SprintAnswerStatus status;

    @Column(name = "marks_awarded", nullable = false)
    @Builder.Default
    private Integer marksAwarded = 0;

    @Column(name = "time_taken_secs")
    private Integer timeTakenSecs;

    @Column(name = "answered_at", nullable = false)
    @Builder.Default
    private Instant answeredAt = Instant.now();
}
