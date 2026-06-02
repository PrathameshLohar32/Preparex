package com.preparex.preparex_backend.service;

import com.preparex.preparex_backend.dto.request.AddContestProblemRequestDto;
import com.preparex.preparex_backend.dto.request.ContestSubmitRequestDto;
import com.preparex.preparex_backend.dto.request.CreateContestRequestDto;
import com.preparex.preparex_backend.dto.response.ContestResponseDto;
import com.preparex.preparex_backend.dto.response.ContestResultResponseDto;
import com.preparex.preparex_backend.dto.response.LeaderboardEntryDto;
import com.preparex.preparex_backend.enums.ContestStatus;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.UUID;

/**
 * Service interface for all contest operations — student and admin flows.
 */
public interface ContestService {

    // ── Student APIs ────────────────────────────────────────────────────

    Page<ContestResponseDto> listContests(ContestStatus status, int page, int size, UUID userId);

    ContestResponseDto getContest(UUID contestId, UUID userId);

    void register(UUID contestId, UUID userId);

    List<Object> getQuestions(UUID contestId, UUID userId);

    void submitAnswer(UUID contestId, UUID userId, ContestSubmitRequestDto request);

    void finalSubmit(UUID contestId, UUID userId);

    List<LeaderboardEntryDto> getLeaderboard(UUID contestId, UUID userId);

    ContestResultResponseDto getMyResult(UUID contestId, UUID userId);

    Page<ContestResultResponseDto> getResults(UUID contestId, int page, int size);

    // ── Admin APIs ──────────────────────────────────────────────────────

    ContestResponseDto createContest(CreateContestRequestDto request);

    void publishContest(UUID contestId);

    void addProblem(UUID contestId, AddContestProblemRequestDto request);

    void forceEndContest(UUID contestId);
}
