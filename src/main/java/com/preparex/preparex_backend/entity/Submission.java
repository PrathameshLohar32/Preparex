package com.preparex.preparex_backend.entity;

import com.preparex.preparex_backend.enums.SubmissionSource;
import com.preparex.preparex_backend.enums.SubmissionStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

/**
 * Submission entity — records a user's attempt at a problem.
 * Scored via the strategy pattern. answer_key is NEVER stored here
 * (only the submitted answer and the scoring result).
 */
@Entity
@Table(
        name = "submissions",
        indexes = {
                @Index(name = "idx_submissions_user_status", columnList = "user_id, status"),
                @Index(name = "idx_submissions_user_problem", columnList = "user_id, problem_id"),
                @Index(name = "idx_submissions_problem_source", columnList = "problem_id, source")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Submission {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "problem_id", nullable = false)
    private Problem problem;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private SubmissionStatus status;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "submitted_answer", nullable = false, columnDefinition = "jsonb")
    private Map<String, Object> submittedAnswer;

    @Column(name = "marks_awarded")
    @Builder.Default
    private Integer marksAwarded = 0;

    @Column(name = "time_taken_secs")
    private Integer timeTakenSecs;

    @Enumerated(EnumType.STRING)
    @Column(name = "source", nullable = false, length = 20)
    private SubmissionSource source;

    @Column(name = "submitted_at")
    @Builder.Default
    private Instant submittedAt = Instant.now();
}
