package com.preparex.preparex_backend.dto.response;

import lombok.*;
import java.util.UUID;

/** Entry in the live leaderboard — from Redis ZSet */
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class LeaderboardEntryDto {
    private UUID userId;
    private String username;
    private Integer score;
    private Long rank;
}
