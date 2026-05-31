package com.preparex.preparex_backend.mapper;

import com.preparex.preparex_backend.dto.request.CreateProblemRequestDto;
import com.preparex.preparex_backend.dto.response.ProblemDetailResponseDto;
import com.preparex.preparex_backend.dto.response.ProblemListItemResponseDto;
import com.preparex.preparex_backend.entity.Problem;
import org.mapstruct.BeanMapping;
import org.mapstruct.Builder;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

/**
 * MapStruct mapper for Problem entity to response DTO conversions.
 * Explicitly ignores answer_key in ALL mappings to prevent accidental exposure.
 */
@Mapper(componentModel = "spring")
public interface ProblemMapper {

    /**
     * Maps Problem to lightweight list item DTO.
     * answer_key is never included in list responses.
     */
    @Mapping(source = "subject.name", target = "subjectName")
    @Mapping(source = "topic.name", target = "topicName")
    @Mapping(target = "isSolved", constant = "false")
    ProblemListItemResponseDto toListItemDto(Problem problem);

    List<ProblemListItemResponseDto> toListItemDtoList(List<Problem> problems);

    /**
     * Maps Problem to full detail DTO.
     * answer_key is explicitly excluded — must NEVER appear in API responses.
     */
    @Mapping(source = "subject.name", target = "subjectName")
    @Mapping(source = "subject.id", target = "subjectId")
    @Mapping(source = "topic.name", target = "topicName")
    @Mapping(source = "topic.id", target = "topicId")
    @Mapping(source = "parent.id", target = "parentId")
    ProblemDetailResponseDto toDetailDto(Problem problem);

    List<ProblemDetailResponseDto> toDetailDtoList(List<Problem> problems);

    /**
     * Maps admin create request to Problem entity.
     * subject, topic, and parent are set manually in the service layer.
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "subject", ignore = true)
    @Mapping(target = "topic", ignore = true)
    @Mapping(target = "parent", ignore = true)
    @Mapping(target = "children", ignore = true)
    @Mapping(target = "isActive", constant = "true")
    @Mapping(target = "attemptCount", constant = "0")
    @Mapping(target = "correctCount", constant = "0")
    @BeanMapping(builder = @Builder(disableBuilder = true))
    Problem fromCreateDto(CreateProblemRequestDto dto);
}
