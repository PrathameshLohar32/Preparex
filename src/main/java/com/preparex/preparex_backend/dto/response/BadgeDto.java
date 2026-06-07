package com.preparex.preparex_backend.dto.response;

import com.preparex.preparex_backend.enums.BadgeType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * Individual badge DTO used in profile responses.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BadgeDto {

    private BadgeType badgeType;
    private String context;
    private Instant awardedAt;
}
