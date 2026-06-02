package com.preparex.preparex_backend.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * Join entity linking a Problem to a Contest with position and per-problem marking.
 */
@Entity
@Table(
        name = "contest_problems",
        uniqueConstraints = @UniqueConstraint(
                name = "uq_cp_contest_problem",
                columnNames = {"contest_id", "problem_id"}
        )
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ContestProblem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", updatable = false, nullable = false)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contest_id", nullable = false)
    private Contest contest;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "problem_id", nullable = false)
    private Problem problem;

    @Column(name = "position")
    @Builder.Default
    private Integer position = 0;

    @Column(name = "marks")
    @Builder.Default
    private Integer marks = 4;

    @Column(name = "negative_marks")
    @Builder.Default
    private Integer negativeMarks = 1;

    @Column(name = "section", length = 50)
    private String section;
}
