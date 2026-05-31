package com.preparex.preparex_backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * Response DTO for subject-wise, difficulty-wise submission statistics.
 * Groups solved counts by subject and difficulty.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubjectStatsResponseDto {

    private List<SubjectStat> subjects;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SubjectStat {
        private String subjectName;
        private Map<String, DifficultyCount> difficulties;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DifficultyCount {
        private Long correct;
        private Long wrong;
        private Long partial;
        private Long total;
    }
}
