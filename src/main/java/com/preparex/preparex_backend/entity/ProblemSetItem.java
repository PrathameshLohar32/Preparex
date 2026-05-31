package com.preparex.preparex_backend.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * Join entity linking a Problem to a ProblemSet with ordering.
 * Does not extend BaseEntity — lightweight join table with SERIAL PK.
 */
@Entity
@Table(
        name = "problem_set_items",
        uniqueConstraints = @UniqueConstraint(
                name = "uq_psi_set_problem",
                columnNames = {"set_id", "problem_id"}
        ),
        indexes = {
                @Index(name = "idx_psi_set_id", columnList = "set_id")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProblemSetItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", updatable = false, nullable = false)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "set_id", nullable = false)
    private ProblemSet problemSet;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "problem_id", nullable = false)
    private Problem problem;

    @Column(name = "position")
    @Builder.Default
    private Integer position = 0;
}
