package com.preparex.preparex_backend.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

/**
 * Final computed contest result for a participant.
 * Created by ContestResultFinalizer after contest ends.
 */
@Entity
@Table(
        name = "contest_results",
        uniqueConstraints = @UniqueConstraint(
                name = "uq_cres_contest_user",
                columnNames = {"contest_id", "user_id"}
        )
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ContestResult {

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

    @Column(name = "total_score")
    @Builder.Default
    private Integer totalScore = 0;

    @Column(name = "rank")
    private Integer rank;

    @Column(name = "percentile")
    private Double percentile;

    @Column(name = "correct_count")
    @Builder.Default
    private Integer correctCount = 0;

    @Column(name = "wrong_count")
    @Builder.Default
    private Integer wrongCount = 0;

    @Column(name = "unattempted_count")
    @Builder.Default
    private Integer unattemptedCount = 0;

    @Column(name = "time_taken_secs")
    @Builder.Default
    private Integer timeTakenSecs = 0;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "subject_breakdown", columnDefinition = "jsonb")
    private Map<String, Object> subjectBreakdown;

    @Column(name = "finalized_at")
    @Builder.Default
    private Instant finalizedAt = Instant.now();
}
