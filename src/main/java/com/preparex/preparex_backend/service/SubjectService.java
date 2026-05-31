package com.preparex.preparex_backend.service;

import com.preparex.preparex_backend.dto.response.SubjectResponseDto;

import java.util.List;

/**
 * Service for subject-related operations.
 */
public interface SubjectService {

    /**
     * Returns all subjects, optionally filtered by exam ID.
     *
     * @param examId optional exam filter (e.g. "JEE", "NEET")
     * @return list of subjects ordered by display_order
     */
    List<SubjectResponseDto> getSubjects(String examId);
}
