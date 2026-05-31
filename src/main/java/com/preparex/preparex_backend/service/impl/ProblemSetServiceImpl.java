package com.preparex.preparex_backend.service.impl;

import com.preparex.preparex_backend.dto.request.AddProblemToSetRequestDto;
import com.preparex.preparex_backend.dto.request.CreateProblemSetRequestDto;
import com.preparex.preparex_backend.dto.response.ProblemSetProgressResponseDto;
import com.preparex.preparex_backend.dto.response.ProblemSetResponseDto;
import com.preparex.preparex_backend.entity.Problem;
import com.preparex.preparex_backend.entity.ProblemSet;
import com.preparex.preparex_backend.entity.ProblemSetItem;
import com.preparex.preparex_backend.exception.DuplicateResourceException;
import com.preparex.preparex_backend.exception.ProblemNotFoundException;
import com.preparex.preparex_backend.exception.ProblemSetNotFoundException;
import com.preparex.preparex_backend.mapper.ProblemSetMapper;
import com.preparex.preparex_backend.repository.ProblemRepository;
import com.preparex.preparex_backend.repository.ProblemSetItemRepository;
import com.preparex.preparex_backend.repository.ProblemSetRepository;
import com.preparex.preparex_backend.service.ProblemSetService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Service implementation for problem set operations.
 * Handles set listings with completion metrics and admin CRUD.
 *
 * <p><strong>Phase 1 note:</strong> completedCount and percentage default to 0
 * since submissions are not yet tracked. These will be populated in Phase 2.</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProblemSetServiceImpl implements ProblemSetService {

    private final ProblemSetRepository problemSetRepository;
    private final ProblemSetItemRepository problemSetItemRepository;
    private final ProblemRepository problemRepository;
    private final ProblemSetMapper problemSetMapper;

    @Override
    public List<ProblemSetResponseDto> getAllSets(UUID userId, boolean isPremiumUser) {
        log.info("Fetching all problem sets for userId={}, isPremium={}", userId, isPremiumUser);

        List<ProblemSet> sets = problemSetRepository.findAllByIsActiveTrueOrderByDisplayOrderAsc();

        return sets.stream()
                .map(set -> buildSetResponseDto(set, isPremiumUser))
                .toList();
    }

    @Override
    public ProblemSetResponseDto getSetBySlug(String slug, UUID userId) {
        log.info("Fetching problem set by slug={} for userId={}", slug, userId);

        ProblemSet set = problemSetRepository.findBySlugAndIsActiveTrue(slug)
                .orElseThrow(() -> new ProblemSetNotFoundException("Problem set not found with slug: " + slug));

        // For now, always treat user as free tier
        return buildSetResponseDto(set, false);
    }

    @Override
    public ProblemSetProgressResponseDto getProgress(String slug, UUID userId) {
        log.info("Fetching progress for set slug={}, userId={}", slug, userId);

        ProblemSet set = problemSetRepository.findBySlugAndIsActiveTrue(slug)
                .orElseThrow(() -> new ProblemSetNotFoundException("Problem set not found with slug: " + slug));

        long total = problemSetItemRepository.countByProblemSetId(set.getId());

        // TODO: Phase 2 — query submissions to compute actual completedCount
        long completed = 0L;
        double percentage = total > 0 ? ((double) completed / total) * 100.0 : 0.0;

        return ProblemSetProgressResponseDto.builder()
                .total(total)
                .completed(completed)
                .percentage(Math.round(percentage * 100.0) / 100.0)
                .build();
    }

    // ── Admin Operations ────────────────────────────────────────────────

    @Override
    @Transactional
    public ProblemSetResponseDto createProblemSet(CreateProblemSetRequestDto dto) {
        log.info("Creating problem set with slug={}", dto.getSlug());

        if (problemSetRepository.existsBySlug(dto.getSlug())) {
            throw new DuplicateResourceException("Problem set slug", dto.getSlug());
        }

        ProblemSet set = problemSetMapper.fromCreateDto(dto);
        ProblemSet saved = problemSetRepository.save(set);

        log.info("Created problem set id={}, slug={}", saved.getId(), saved.getSlug());
        return buildSetResponseDto(saved, true);
    }

    @Override
    @Transactional
    public void addProblemToSet(UUID setId, AddProblemToSetRequestDto dto) {
        log.info("Adding problem {} to set {} at position {}",
                dto.getProblemId(), setId, dto.getPosition());

        ProblemSet set = problemSetRepository.findById(setId)
                .orElseThrow(() -> new ProblemSetNotFoundException("Problem set not found with id: " + setId));

        Problem problem = problemRepository.findByIdAndIsActiveTrue(dto.getProblemId())
                .orElseThrow(() -> new ProblemNotFoundException("Problem not found with id: " + dto.getProblemId()));

        if (problemSetItemRepository.existsByProblemSetIdAndProblemId(setId, dto.getProblemId())) {
            throw new DuplicateResourceException("Problem in set", dto.getProblemId().toString());
        }

        ProblemSetItem item = ProblemSetItem.builder()
                .problemSet(set)
                .problem(problem)
                .position(dto.getPosition())
                .build();

        problemSetItemRepository.save(item);
        log.info("Added problem {} to set {} at position {}", dto.getProblemId(), setId, dto.getPosition());
    }

    // ── Private Helpers ─────────────────────────────────────────────────

    private ProblemSetResponseDto buildSetResponseDto(ProblemSet set, boolean isPremiumUser) {
        ProblemSetResponseDto dto = problemSetMapper.toResponseDto(set);
        long total = problemSetItemRepository.countByProblemSetId(set.getId());
        dto.setProblemCount(total);

        // TODO: Phase 2 — compute completedCount from submissions
        dto.setCompletedCount(0L);
        dto.setPercentage(0.0);

        // Lock premium sets for free users
        dto.setLocked(set.getIsPremium() && !isPremiumUser);

        return dto;
    }
}
