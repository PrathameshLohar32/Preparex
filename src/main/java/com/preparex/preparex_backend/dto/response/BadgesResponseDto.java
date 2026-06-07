package com.preparex.preparex_backend.dto.response;

import com.preparex.preparex_backend.enums.BadgeType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Badges response with earned and locked badge lists.
 * Locked = all BadgeType values minus earned.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BadgesResponseDto {

    private List<BadgeDto> earned;
    private List<BadgeType> locked;
}
