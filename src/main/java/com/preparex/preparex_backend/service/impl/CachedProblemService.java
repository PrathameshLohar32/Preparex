package com.preparex.preparex_backend.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.preparex.preparex_backend.constant.RedisKeyConstants;
import com.preparex.preparex_backend.dto.request.CreateProblemRequestDto;
import com.preparex.preparex_backend.dto.request.ProblemFilterRequestDto;
import com.preparex.preparex_backend.dto.request.UpdateProblemRequestDto;
import com.preparex.preparex_backend.dto.response.PassageResponseDto;
import com.preparex.preparex_backend.dto.response.ProblemDetailResponseDto;
import com.preparex.preparex_backend.dto.response.ProblemListItemResponseDto;
import com.preparex.preparex_backend.service.ProblemService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Primary;
import org.springframework.data.domain.Page;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * Decorator around ProblemServiceImpl that adds Redis caching.
 * Marked @Primary so controllers and other consumers get the cached version.
 *
 * <p>Caching strategy:</p>
 * <ul>
 *   <li>getProblemById: cached under "problem:{id}" with 1hr TTL</li>
 *   <li>updateProblem/deleteProblem: evicts the cache on success</li>
 *   <li>Listing, passage, hints, solution: not cached (low benefit vs. complexity)</li>
 * </ul>
 */
@Slf4j
@Service
@Primary
public class CachedProblemService implements ProblemService {

    private final ProblemService delegate;
    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;

    public CachedProblemService(
            @Qualifier("problemServiceImpl") ProblemService delegate,
            RedisTemplate<String, Object> redisTemplate,
            ObjectMapper objectMapper) {
        this.delegate = delegate;
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
    }

    @Override
    public Page<ProblemListItemResponseDto> getProblems(ProblemFilterRequestDto filter, UUID userId) {
        return delegate.getProblems(filter, userId);
    }

    /**
     * Checks Redis cache first; falls through to DB on miss.
     * Caches the result for 1 hour.
     */
    @Override
    public ProblemDetailResponseDto getProblemById(UUID id, UUID userId) {
        String cacheKey = RedisKeyConstants.problemCacheKey(id.toString());

        try {
            Object cached = redisTemplate.opsForValue().get(cacheKey);
            if (cached != null) {
                log.debug("Cache HIT for problem id={}", id);
                String json = objectMapper.writeValueAsString(cached);
                return objectMapper.readValue(json, ProblemDetailResponseDto.class);
            }
        } catch (JsonProcessingException e) {
            log.warn("Failed to deserialize cached problem id={}, fetching from DB", id, e);
        }

        log.debug("Cache MISS for problem id={}", id);
        ProblemDetailResponseDto result = delegate.getProblemById(id, userId);

        try {
            redisTemplate.opsForValue().set(
                    cacheKey,
                    result,
                    RedisKeyConstants.PROBLEM_CACHE_TTL_HOURS,
                    TimeUnit.HOURS
            );
            log.debug("Cached problem id={} with TTL={}h", id, RedisKeyConstants.PROBLEM_CACHE_TTL_HOURS);
        } catch (Exception e) {
            log.warn("Failed to cache problem id={}", id, e);
        }

        return result;
    }

    @Override
    public PassageResponseDto getPassage(UUID parentId) {
        return delegate.getPassage(parentId);
    }

    @Override
    public List<String> getHints(UUID problemId) {
        return delegate.getHints(problemId);
    }

    @Override
    public ProblemDetailResponseDto getSolution(UUID problemId, UUID userId) {
        return delegate.getSolution(problemId, userId);
    }

    @Override
    public ProblemDetailResponseDto createProblem(CreateProblemRequestDto dto) {
        return delegate.createProblem(dto);
    }

    /**
     * Delegates update and evicts the cache for the updated problem.
     */
    @Override
    public ProblemDetailResponseDto updateProblem(UUID id, UpdateProblemRequestDto dto) {
        ProblemDetailResponseDto result = delegate.updateProblem(id, dto);
        evictCache(id);
        return result;
    }

    /**
     * Delegates soft delete and evicts the cache.
     */
    @Override
    public void deleteProblem(UUID id) {
        delegate.deleteProblem(id);
        evictCache(id);
    }

    private void evictCache(UUID problemId) {
        String cacheKey = RedisKeyConstants.problemCacheKey(problemId.toString());
        Boolean deleted = redisTemplate.delete(cacheKey);
        log.info("Evicted cache for problem id={}, existed={}", problemId, deleted);
    }
}
