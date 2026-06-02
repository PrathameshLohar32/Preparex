package com.preparex.preparex_backend.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.*;
import java.util.Map;
import java.util.UUID;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class ContestSubmitRequestDto {
    @NotNull(message = "Problem ID is required") private UUID problemId;
    @NotNull(message = "Answer is required") private Map<String, Object> answer;
    private Integer timeTakenSecs;
}
