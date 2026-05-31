package com.preparex.preparex_backend.entity;

import com.preparex.preparex_backend.common.BaseEntity;
import com.preparex.preparex_backend.enums.Difficulty;
import com.preparex.preparex_backend.enums.QuestionType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Core problem entity supporting multiple question types.
 * Uses JSONB columns for flexible options, answer keys, and hints storage.
 * Supports self-referencing parent-child relationship for passage-type questions.
 *
 * <p><strong>IMPORTANT:</strong> answer_key must NEVER be exposed in any API response.</p>
 */
@Entity
@Table(
        name = "problems",
        indexes = {
                @Index(name = "idx_problems_topic_difficulty", columnList = "topic_id, difficulty"),
                @Index(name = "idx_problems_subject_difficulty", columnList = "subject_id, difficulty"),
                @Index(name = "idx_problems_is_active", columnList = "is_active"),
                @Index(name = "idx_problems_exam_id", columnList = "exam_id"),
                @Index(name = "idx_problems_parent_id", columnList = "parent_id"),
                @Index(name = "idx_problems_slug", columnList = "slug")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Problem extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "slug", nullable = false, unique = true, length = 200)
    private String slug;

    @Column(name = "title", nullable = false, length = 500)
    private String title;

    @Column(name = "body_text", nullable = false, columnDefinition = "TEXT")
    private String bodyText;

    @Column(name = "figure_url", length = 500)
    private String figureUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "question_type", nullable = false, length = 30)
    private QuestionType questionType;

    @Enumerated(EnumType.STRING)
    @Column(name = "difficulty", nullable = false, length = 10)
    private Difficulty difficulty;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "options", columnDefinition = "jsonb")
    private List<Map<String, Object>> options;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "answer_key", nullable = false, columnDefinition = "jsonb")
    private Map<String, Object> answerKey;

    @Column(name = "solution_text", columnDefinition = "TEXT")
    private String solutionText;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "hints", columnDefinition = "jsonb")
    private List<String> hints;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subject_id")
    private Subject subject;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "topic_id")
    private Topic topic;

    @Column(name = "exam_id", nullable = false, length = 50)
    private String examId;

    @Column(name = "pyq_year")
    private Integer pyqYear;

    /** Self-join: parent problem for PARAGRAPH-type child questions */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Problem parent;

    /** Children questions under a PARAGRAPH-type parent */
    @OneToMany(mappedBy = "parent", fetch = FetchType.LAZY)
    @Builder.Default
    private List<Problem> children = new ArrayList<>();

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    @Column(name = "is_premium", nullable = false)
    @Builder.Default
    private Boolean isPremium = false;

    @Column(name = "attempt_count", nullable = false)
    @Builder.Default
    private Integer attemptCount = 0;

    @Column(name = "correct_count", nullable = false)
    @Builder.Default
    private Integer correctCount = 0;
}
