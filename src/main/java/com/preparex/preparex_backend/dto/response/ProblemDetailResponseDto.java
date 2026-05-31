package com.preparex.preparex_backend.dto.response;

import com.preparex.preparex_backend.enums.Difficulty;
import com.preparex.preparex_backend.enums.QuestionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Full problem detail response.
 * Includes all fields EXCEPT answer_key, which must NEVER be exposed.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProblemDetailResponseDto {

    private UUID id;
    private String slug;
    private String title;
    private String bodyText;
    private String figureUrl;
    private QuestionType questionType;
    private Difficulty difficulty;
    private List<Map<String, Object>> options;
    private String solutionText;
    private List<String> hints;
    private String subjectName;
    private Integer subjectId;
    private String topicName;
    private Integer topicId;
    private String examId;
    private Integer pyqYear;
    private Boolean isPremium;
    private Integer attemptCount;
    private Integer correctCount;
    private UUID parentId;
}
