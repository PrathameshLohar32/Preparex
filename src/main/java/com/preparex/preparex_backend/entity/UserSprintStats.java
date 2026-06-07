package com.preparex.preparex_backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

/**
 * User sprint stats entity — aggregate sprint performance metrics.
 * One row per user. Updated asynchronously by SprintStatsConsumer via Kafka.
 */
@Entity
@Table(name = "user_sprint_stats")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserSprintStats {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(name = "total_sprints", nullable = false)
    @Builder.Default
    private Integer totalSprints = 0;

    @Column(name = "total_points", nullable = false)
    @Builder.Default
    private Integer totalPoints = 0;

    @Column(name = "best_weekly_rank")
    private Integer bestWeeklyRank;

    @Column(name = "updated_at")
    @Builder.Default
    private Instant updatedAt = Instant.now();
}
