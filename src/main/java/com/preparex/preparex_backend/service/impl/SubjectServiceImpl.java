package com.preparex.preparex_backend.service.impl;

import com.preparex.preparex_backend.dto.response.SubjectResponseDto;
import com.preparex.preparex_backend.entity.Subject;
import com.preparex.preparex_backend.mapper.SubjectMapper;
import com.preparex.preparex_backend.repository.SubjectRepository;
import com.preparex.preparex_backend.service.SubjectService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * Service implementation for subject operations.
 * Provides filtered subject listings ordered by display_order.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SubjectServiceImpl implements SubjectService {

    private final SubjectRepository subjectRepository;
    private final SubjectMapper subjectMapper;

    @Override
    public List<SubjectResponseDto> getSubjects(String examId) {
        log.info("Fetching subjects with examId={}", examId);

        List<Subject> subjects;
        if (StringUtils.hasText(examId)) {
            subjects = subjectRepository.findByExamIdOrderByDisplayOrderAsc(examId);
        } else {
            subjects = subjectRepository.findAllByOrderByDisplayOrderAsc();
        }

        log.info("Found {} subjects", subjects.size());
        return subjectMapper.toResponseDtoList(subjects);
    }
}
