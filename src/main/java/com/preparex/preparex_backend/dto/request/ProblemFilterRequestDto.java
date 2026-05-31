package com.preparex.preparex_backend.dto.request;

import com.preparex.preparex_backend.enums.Difficulty;
import com.preparex.preparex_backend.enums.QuestionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Filter parameters for the paginated problem listing endpoint.
 * All fields are optional — null values are ignored in the query.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProblemFilterRequestDto {

    private Integer subjectId;

    private Integer topicId;

    private Difficulty difficulty;

    private QuestionType questionType;

    private String examId;

    @Builder.Default
    private Boolean pyqOnly = false;

    @Builder.Default
    private Integer page = 0;

    @Builder.Default
    private Integer size = 20;
}
