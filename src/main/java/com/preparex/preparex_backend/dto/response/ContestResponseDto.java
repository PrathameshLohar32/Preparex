package com.preparex.preparex_backend.dto.response;

import com.preparex.preparex_backend.enums.AccessType;
import com.preparex.preparex_backend.enums.ContestStatus;
import com.preparex.preparex_backend.enums.ContestType;
import lombok.*;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class ContestResponseDto {
    private UUID id;
    private String title;
    private String description;
    private ContestType type;
    private ContestStatus status;
    private String examId;
    private Instant startsAt;
    private Instant endsAt;
    private Integer durationMins;
    private Map<String, Object> markingScheme;
    private AccessType accessType;
    private Long registeredCount;
    private Long problemCount;
    private Boolean isRegistered;
}
