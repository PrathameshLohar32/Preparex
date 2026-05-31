package com.preparex.preparex_backend.service;

import com.preparex.preparex_backend.dto.response.TopicResponseDto;

import java.util.List;

/**
 * Service for topic-related operations.
 */
public interface TopicService {

    /**
     * Returns all topics, optionally filtered by subject ID.
     *
     * @param subjectId optional subject filter
     * @return list of topics ordered by display_order
     */
    List<TopicResponseDto> getTopics(Integer subjectId);
}
