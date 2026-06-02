package com.preparex.preparex_backend.entity;

import com.preparex.preparex_backend.enums.SubmissionStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

/**
 * Per-question submission within a contest.
 * Unique constraint on (contest_id, user_id, problem_id) — one answer per problem per user.
 * Reuses SubmissionStatus enum from Phase 2.
 */
@Entity
@Table(
        name = "contest_submissions",
        uniqueConstraints = @UniqueConstraint(
                name = "uq_cs_contest_user_problem",
                columnNames = {"contest_id", "user_id", "problem_id"}
        )
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ContestSubmission {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contest_id", nullable = false)
    private Contest contest;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "problem_id", nullable = false)
    private Problem problem;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20)
    private SubmissionStatus status;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "submitted_answer", columnDefinition = "jsonb")
    private Map<String, Object> submittedAnswer;

    @Column(name = "marks_awarded")
    @Builder.Default
    private Integer marksAwarded = 0;

    @Column(name = "time_taken_secs")
    private Integer timeTakenSecs;

    @Column(name = "submitted_at")
    @Builder.Default
    private Instant submittedAt = Instant.now();
}
