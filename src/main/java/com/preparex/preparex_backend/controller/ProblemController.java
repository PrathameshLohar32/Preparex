package com.preparex.preparex_backend.controller;

import com.preparex.preparex_backend.common.ApiResponse;
import com.preparex.preparex_backend.dto.request.ProblemFilterRequestDto;
import com.preparex.preparex_backend.dto.response.PassageResponseDto;
import com.preparex.preparex_backend.dto.response.ProblemDetailResponseDto;
import com.preparex.preparex_backend.dto.response.ProblemListItemResponseDto;
import com.preparex.preparex_backend.dto.response.SubjectResponseDto;
import com.preparex.preparex_backend.dto.response.TopicResponseDto;
import com.preparex.preparex_backend.enums.Difficulty;
import com.preparex.preparex_backend.enums.QuestionType;
import com.preparex.preparex_backend.security.CustomUserDetails;
import com.preparex.preparex_backend.service.ProblemService;
import com.preparex.preparex_backend.service.SubjectService;
import com.preparex.preparex_backend.service.TopicService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * Public problem endpoints for authenticated users.
 * Provides listing with filters, problem detail, passage, hints, and solution.
 * Also serves subject and topic reference data.
 */
@Slf4j
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Tag(name = "Problems", description = "Problem listing, detail, passage, hints, and solution APIs")
public class ProblemController {

    private final ProblemService problemService;
    private final SubjectService subjectService;
    private final TopicService topicService;

    @Operation(
            summary = "List problems with filters",
            description = "Returns paginated problems filtered by subject, topic, difficulty, type, exam, and PYQ. Max page size: 50."
    )
    @GetMapping("/problems")
    public ResponseEntity<ApiResponse<Page<ProblemListItemResponseDto>>> getProblems(
            @RequestParam(required = false) Integer subjectId,
            @RequestParam(required = false) Integer topicId,
            @RequestParam(required = false) Difficulty difficulty,
            @RequestParam(required = false) QuestionType type,
            @RequestParam(required = false) String examId,
            @RequestParam(required = false, defaultValue = "false") Boolean pyqOnly,
            @RequestParam(required = false, defaultValue = "0") Integer page,
            @RequestParam(required = false, defaultValue = "20") Integer size,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        ProblemFilterRequestDto filter = ProblemFilterRequestDto.builder()
                .subjectId(subjectId)
                .topicId(topicId)
                .difficulty(difficulty)
                .questionType(type)
                .examId(examId)
                .pyqOnly(pyqOnly)
                .page(page)
                .size(size)
                .build();

        Page<ProblemListItemResponseDto> result = problemService.getProblems(filter, userDetails.getUserId());
        return ResponseEntity.ok(ApiResponse.success("Problems fetched successfully", result));
    }

    @Operation(
            summary = "Get problem detail",
            description = "Returns full problem detail (without answer_key). Redis cached for 1 hour."
    )
    @GetMapping("/problems/{id}")
    public ResponseEntity<ApiResponse<ProblemDetailResponseDto>> getProblemById(
            @PathVariable UUID id,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        ProblemDetailResponseDto result = problemService.getProblemById(id, userDetails.getUserId());
        return ResponseEntity.ok(ApiResponse.success("Problem fetched successfully", result));
    }

    @Operation(
            summary = "Get problem solution",
            description = "Returns solution text. In Phase 2, requires at least one submission."
    )
    @GetMapping("/problems/{id}/solution")
    public ResponseEntity<ApiResponse<ProblemDetailResponseDto>> getSolution(
            @PathVariable UUID id,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        ProblemDetailResponseDto result = problemService.getSolution(id, userDetails.getUserId());
        return ResponseEntity.ok(ApiResponse.success("Solution fetched successfully", result));
    }

    @Operation(
            summary = "Get problem hints",
            description = "Returns hints array. Client reveals one at a time."
    )
    @GetMapping("/problems/{id}/hints")
    public ResponseEntity<ApiResponse<List<String>>> getHints(
            @PathVariable UUID id,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        List<String> hints = problemService.getHints(id);
        return ResponseEntity.ok(ApiResponse.success("Hints fetched successfully", hints));
    }

    @Operation(
            summary = "Get passage with children",
            description = "Returns the parent passage problem and all child questions in one response."
    )
    @GetMapping("/problems/passage/{parentId}")
    public ResponseEntity<ApiResponse<PassageResponseDto>> getPassage(
            @PathVariable UUID parentId) {

        PassageResponseDto result = problemService.getPassage(parentId);
        return ResponseEntity.ok(ApiResponse.success("Passage fetched successfully", result));
    }

    @Operation(summary = "List subjects", description = "Returns subjects optionally filtered by exam ID")
    @GetMapping("/subjects")
    public ResponseEntity<ApiResponse<List<SubjectResponseDto>>> getSubjects(
            @RequestParam(required = false) String examId) {

        List<SubjectResponseDto> result = subjectService.getSubjects(examId);
        return ResponseEntity.ok(ApiResponse.success("Subjects fetched successfully", result));
    }

    @Operation(summary = "List topics", description = "Returns topics optionally filtered by subject ID")
    @GetMapping("/topics")
    public ResponseEntity<ApiResponse<List<TopicResponseDto>>> getTopics(
            @RequestParam(required = false) Integer subjectId) {

        List<TopicResponseDto> result = topicService.getTopics(subjectId);
        return ResponseEntity.ok(ApiResponse.success("Topics fetched successfully", result));
    }
}
