package com.preparex.preparex_backend.dto.request;

import com.preparex.preparex_backend.enums.AccessType;
import com.preparex.preparex_backend.enums.ContestType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class CreateContestRequestDto {
    @NotBlank(message = "Title is required") private String title;
    private String description;
    @NotNull(message = "Contest type is required") private ContestType type;
    private String examId;
    private Instant startsAt;
    private Instant endsAt;
    private Integer durationMins;
    private Map<String, Object> markingScheme;
    @Builder.Default private AccessType accessType = AccessType.FREE;
    private BigDecimal paidAmountInr;
    private Integer maxParticipants;
}
