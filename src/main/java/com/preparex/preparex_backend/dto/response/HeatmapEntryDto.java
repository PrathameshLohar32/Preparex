package com.preparex.preparex_backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * Heatmap entry for activity visualization — one entry per day.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HeatmapEntryDto {

    private LocalDate date;
    private int count;
}
