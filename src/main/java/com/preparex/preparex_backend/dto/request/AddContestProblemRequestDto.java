package com.preparex.preparex_backend.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.*;
import java.util.UUID;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class AddContestProblemRequestDto {
    @NotNull(message = "Problem ID is required") private UUID problemId;
    @Builder.Default private Integer position = 0;
    @Builder.Default private Integer marks = 4;
    @Builder.Default private Integer negativeMarks = 1;
    private String section;
}
