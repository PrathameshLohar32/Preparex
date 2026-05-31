package com.preparex.preparex_backend.entity;

import com.preparex.preparex_backend.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

/**
 * Subject entity representing exam-specific subject categories.
 * Examples: Physics (JEE), Chemistry (NEET).
 */
@Entity
@Table(
        name = "subjects",
        uniqueConstraints = @UniqueConstraint(
                name = "uq_subjects_name_exam",
                columnNames = {"name", "exam_id"}
        )
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Subject extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", updatable = false, nullable = false)
    private Integer id;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "exam_id", nullable = false, length = 50)
    private String examId;

    @Column(name = "display_order")
    @Builder.Default
    private Integer displayOrder = 0;
}
