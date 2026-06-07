package com.preparex.preparex_backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

/**
 * User solved stats entity — denormalized solved counts per difficulty level.
 * Updated asynchronously by ProfileStatsConsumer via Kafka.
 */
@Entity
@Table(name = "user_solved_stats")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserSolvedStats {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(name = "total", nullable = false)
    @Builder.Default
    private Integer total = 0;

    @Column(name = "easy", nullable = false)
    @Builder.Default
    private Integer easy = 0;

    @Column(name = "medium", nullable = false)
    @Builder.Default
    private Integer medium = 0;

    @Column(name = "hard", nullable = false)
    @Builder.Default
    private Integer hard = 0;

    @Column(name = "updated_at")
    @Builder.Default
    private Instant updatedAt = Instant.now();
}
