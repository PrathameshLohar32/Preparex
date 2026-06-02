package com.preparex.preparex_backend.service.contest;

import com.preparex.preparex_backend.dto.response.LeaderboardEntryDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

/**
 * Broadcasts leaderboard updates to WebSocket subscribers.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class LeaderboardBroadcaster {

    private final SimpMessagingTemplate messagingTemplate;

    public void broadcastUpdate(UUID contestId, List<LeaderboardEntryDto> top50) {
        String destination = "/topic/contest/" + contestId + "/leaderboard";
        messagingTemplate.convertAndSend(destination, top50);
        log.debug("Broadcast leaderboard update to {} ({} entries)", destination, top50.size());
    }
}
