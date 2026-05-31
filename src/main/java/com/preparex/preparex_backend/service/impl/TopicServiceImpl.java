package com.preparex.preparex_backend.service.impl;

import com.preparex.preparex_backend.dto.response.TopicResponseDto;
import com.preparex.preparex_backend.entity.Topic;
import com.preparex.preparex_backend.mapper.TopicMapper;
import com.preparex.preparex_backend.repository.TopicRepository;
import com.preparex.preparex_backend.service.TopicService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service implementation for topic operations.
 * Provides filtered topic listings ordered by display_order.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TopicServiceImpl implements TopicService {

    private final TopicRepository topicRepository;
    private final TopicMapper topicMapper;

    @Override
    public List<TopicResponseDto> getTopics(Integer subjectId) {
        log.info("Fetching topics with subjectId={}", subjectId);

        List<Topic> topics;
        if (subjectId != null) {
            topics = topicRepository.findBySubjectIdOrderByDisplayOrderAsc(subjectId);
        } else {
            topics = topicRepository.findAllByOrderByDisplayOrderAsc();
        }

        log.info("Found {} topics", topics.size());
        return topicMapper.toResponseDtoList(topics);
    }
}
