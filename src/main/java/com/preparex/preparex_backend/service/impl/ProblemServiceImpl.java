package com.preparex.preparex_backend.service.impl;

import com.preparex.preparex_backend.dto.request.CreateProblemRequestDto;
import com.preparex.preparex_backend.dto.request.ProblemFilterRequestDto;
import com.preparex.preparex_backend.dto.request.UpdateProblemRequestDto;
import com.preparex.preparex_backend.dto.response.PassageResponseDto;
import com.preparex.preparex_backend.dto.response.ProblemDetailResponseDto;
import com.preparex.preparex_backend.dto.response.ProblemListItemResponseDto;
import com.preparex.preparex_backend.entity.Problem;
import com.preparex.preparex_backend.entity.Subject;
import com.preparex.preparex_backend.entity.Topic;
import com.preparex.preparex_backend.exception.ProblemNotFoundException;
import com.preparex.preparex_backend.mapper.ProblemMapper;
import com.preparex.preparex_backend.repository.ProblemRepository;
import com.preparex.preparex_backend.repository.SubjectRepository;
import com.preparex.preparex_backend.repository.TopicRepository;
import com.preparex.preparex_backend.service.PremiumAccessGuard;
import com.preparex.preparex_backend.service.ProblemService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.UUID;

/**
 * Core implementation of ProblemService.
 * Handles business logic for problem CRUD and retrieval.
 *
 * <p>This bean is NOT @Primary — the CachedProblemService decorator
 * wraps this and is injected into controllers.</p>
 */
@Slf4j
@Service("problemServiceImpl")
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProblemServiceImpl implements ProblemService {

    private static final int MAX_PAGE_SIZE = 50;
    private static final int DEFAULT_PAGE_SIZE = 20;

    private final ProblemRepository problemRepository;
    private final SubjectRepository subjectRepository;
    private final TopicRepository topicRepository;
    private final ProblemMapper problemMapper;
    private final PremiumAccessGuard premiumAccessGuard;

    @Override
    public Page<ProblemListItemResponseDto> getProblems(ProblemFilterRequestDto filter, UUID userId) {
        log.info("Fetching problems with filters: subject={}, topic={}, difficulty={}, type={}, exam={}, pyq={}",
                filter.getSubjectId(), filter.getTopicId(), filter.getDifficulty(),
                filter.getQuestionType(), filter.getExamId(), filter.getPyqOnly());

        int pageSize = Math.min(
                filter.getSize() != null ? filter.getSize() : DEFAULT_PAGE_SIZE,
                MAX_PAGE_SIZE
        );
        int pageNumber = filter.getPage() != null ? filter.getPage() : 0;
        Pageable pageable = PageRequest.of(pageNumber, pageSize);

        boolean pyqOnly = filter.getPyqOnly() != null && filter.getPyqOnly();

        Page<Problem> problems = problemRepository.findByFilters(
                filter.getSubjectId(),
                filter.getTopicId(),
                filter.getDifficulty(),
                filter.getQuestionType(),
                filter.getExamId(),
                pyqOnly,
                pageable
        );

        log.info("Found {} problems (page {}/{})", problems.getNumberOfElements(),
                pageNumber, problems.getTotalPages());

        return problems.map(problemMapper::toListItemDto);
    }

    @Override
    public ProblemDetailResponseDto getProblemById(UUID id, UUID userId) {
        log.info("Fetching problem detail for id={}", id);

        Problem problem = findActiveByIdOrThrow(id);
        premiumAccessGuard.checkAccess(userId, problem.getIsPremium());

        return problemMapper.toDetailDto(problem);
    }

    @Override
    public PassageResponseDto getPassage(UUID parentId) {
        log.info("Fetching passage for parentId={}", parentId);

        Problem parent = findActiveByIdOrThrow(parentId);
        List<Problem> children = problemRepository.findByParentIdAndIsActiveTrueOrderBySlugAsc(parentId);

        return PassageResponseDto.builder()
                .parent(problemMapper.toDetailDto(parent))
                .children(problemMapper.toDetailDtoList(children))
                .build();
    }

    @Override
    public List<String> getHints(UUID problemId) {
        log.info("Fetching hints for problemId={}", problemId);

        Problem problem = findActiveByIdOrThrow(problemId);
        return problem.getHints() != null ? problem.getHints() : List.of();
    }

    @Override
    public ProblemDetailResponseDto getSolution(UUID problemId, UUID userId) {
        log.info("Fetching solution for problemId={}, userId={}", problemId, userId);

        Problem problem = findActiveByIdOrThrow(problemId);

        // TODO: Phase 2 — check if user has at least one submission for this problem
        // For now, return solution unconditionally

        ProblemDetailResponseDto dto = problemMapper.toDetailDto(problem);
        dto.setSolutionText(problem.getSolutionText());
        return dto;
    }

    // ── Admin Operations ────────────────────────────────────────────────

    @Override
    @Transactional
    public ProblemDetailResponseDto createProblem(CreateProblemRequestDto dto) {
        log.info("Creating problem with slug={}", dto.getSlug());

        Problem problem = problemMapper.fromCreateDto(dto);

        resolveRelationships(problem, dto.getSubjectId(), dto.getTopicId(), dto.getParentId());

        Problem saved = problemRepository.save(problem);
        log.info("Created problem id={}, slug={}", saved.getId(), saved.getSlug());

        return problemMapper.toDetailDto(saved);
    }

    @Override
    @Transactional
    public ProblemDetailResponseDto updateProblem(UUID id, UpdateProblemRequestDto dto) {
        log.info("Updating problem id={}", id);

        Problem problem = findActiveByIdOrThrow(id);

        applyUpdates(problem, dto);

        Problem saved = problemRepository.save(problem);
        log.info("Updated problem id={}, slug={}", saved.getId(), saved.getSlug());

        return problemMapper.toDetailDto(saved);
    }

    @Override
    @Transactional
    public void deleteProblem(UUID id) {
        log.info("Soft-deleting problem id={}", id);

        Problem problem = findActiveByIdOrThrow(id);
        problem.setIsActive(false);
        problemRepository.save(problem);

        log.info("Soft-deleted problem id={}", id);
    }

    // ── Private Helpers ─────────────────────────────────────────────────

    private Problem findActiveByIdOrThrow(UUID id) {
        return problemRepository.findByIdAndIsActiveTrue(id)
                .orElseThrow(() -> new ProblemNotFoundException("Problem not found with id: " + id));
    }

    /**
     * Resolves FK relationships for subject, topic, and parent.
     */
    private void resolveRelationships(Problem problem, Integer subjectId, Integer topicId, String parentId) {
        if (subjectId != null) {
            Subject subject = subjectRepository.findById(subjectId)
                    .orElseThrow(() -> new ProblemNotFoundException("Subject not found with id: " + subjectId));
            problem.setSubject(subject);
        }

        if (topicId != null) {
            Topic topic = topicRepository.findById(topicId)
                    .orElseThrow(() -> new ProblemNotFoundException("Topic not found with id: " + topicId));
            problem.setTopic(topic);
        }

        if (StringUtils.hasText(parentId)) {
            Problem parent = findActiveByIdOrThrow(UUID.fromString(parentId));
            problem.setParent(parent);
        }
    }

    /**
     * Applies non-null fields from the update DTO to the existing problem.
     */
    private void applyUpdates(Problem problem, UpdateProblemRequestDto dto) {
        if (StringUtils.hasText(dto.getTitle())) {
            problem.setTitle(dto.getTitle());
        }
        if (StringUtils.hasText(dto.getBodyText())) {
            problem.setBodyText(dto.getBodyText());
        }
        if (dto.getFigureUrl() != null) {
            problem.setFigureUrl(dto.getFigureUrl());
        }
        if (dto.getQuestionType() != null) {
            problem.setQuestionType(dto.getQuestionType());
        }
        if (dto.getDifficulty() != null) {
            problem.setDifficulty(dto.getDifficulty());
        }
        if (dto.getOptions() != null) {
            problem.setOptions(dto.getOptions());
        }
        if (dto.getAnswerKey() != null) {
            problem.setAnswerKey(dto.getAnswerKey());
        }
        if (dto.getSolutionText() != null) {
            problem.setSolutionText(dto.getSolutionText());
        }
        if (dto.getHints() != null) {
            problem.setHints(dto.getHints());
        }
        if (dto.getExamId() != null) {
            problem.setExamId(dto.getExamId());
        }
        if (dto.getPyqYear() != null) {
            problem.setPyqYear(dto.getPyqYear());
        }
        if (dto.getIsPremium() != null) {
            problem.setIsPremium(dto.getIsPremium());
        }
        if (dto.getSubjectId() != null) {
            Subject subject = subjectRepository.findById(dto.getSubjectId())
                    .orElseThrow(() -> new ProblemNotFoundException("Subject not found with id: " + dto.getSubjectId()));
            problem.setSubject(subject);
        }
        if (dto.getTopicId() != null) {
            Topic topic = topicRepository.findById(dto.getTopicId())
                    .orElseThrow(() -> new ProblemNotFoundException("Topic not found with id: " + dto.getTopicId()));
            problem.setTopic(topic);
        }
    }
}
