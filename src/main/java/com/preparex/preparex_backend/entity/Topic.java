package com.preparex.preparex_backend.entity;

import com.preparex.preparex_backend.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

/**
 * Topic entity representing a specific topic within a subject.
 * Examples: Kinematics under Physics, Organic Chemistry under Chemistry.
 */
@Entity
@Table(
        name = "topics",
        indexes = {
                @Index(name = "idx_topics_subject_id", columnList = "subject_id")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Topic extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", updatable = false, nullable = false)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subject_id", nullable = false)
    private Subject subject;

    @Column(name = "name", nullable = false, length = 150)
    private String name;

    @Column(name = "display_order")
    @Builder.Default
    private Integer displayOrder = 0;
}
