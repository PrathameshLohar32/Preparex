package com.preparex.preparex_backend.entity;

import com.preparex.preparex_backend.enums.AccessType;
import com.preparex.preparex_backend.enums.ContestStatus;
import com.preparex.preparex_backend.enums.ContestType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Contest entity with state machine lifecycle.
 * Transitions: DRAFT→SCHEDULED→LIVE→ENDED→RESULTS_PUBLISHED | ANY→CANCELLED
 */
@Entity
@Table(
        name = "contests",
        indexes = {
                @Index(name = "idx_contests_status", columnList = "status"),
                @Index(name = "idx_contests_starts_at", columnList = "starts_at")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Contest {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "title", nullable = false, length = 255)
    private String title;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 30)
    private ContestType type;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 25)
    @Builder.Default
    private ContestStatus status = ContestStatus.DRAFT;

    @Column(name = "exam_id", length = 50)
    private String examId;

    @Column(name = "starts_at")
    private Instant startsAt;

    @Column(name = "ends_at")
    private Instant endsAt;

    @Column(name = "duration_mins")
    private Integer durationMins;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "marking_scheme", nullable = false, columnDefinition = "jsonb")
    @Builder.Default
    private Map<String, Object> markingScheme = Map.of("correct", 4, "wrong", -1, "unattempted", 0);

    @Enumerated(EnumType.STRING)
    @Column(name = "access_type", length = 20)
    @Builder.Default
    private AccessType accessType = AccessType.FREE;

    @Column(name = "paid_amount_inr", precision = 8, scale = 2)
    private BigDecimal paidAmountInr;

    @Column(name = "max_participants")
    private Integer maxParticipants;

    @Column(name = "created_at")
    @Builder.Default
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at")
    @Builder.Default
    private Instant updatedAt = Instant.now();

    @OneToMany(mappedBy = "contest", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @Builder.Default
    private List<ContestProblem> problems = new ArrayList<>();
}
