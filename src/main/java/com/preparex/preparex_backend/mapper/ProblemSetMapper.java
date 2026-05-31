package com.preparex.preparex_backend.mapper;

import com.preparex.preparex_backend.dto.request.CreateProblemSetRequestDto;
import com.preparex.preparex_backend.dto.response.ProblemSetResponseDto;
import com.preparex.preparex_backend.entity.ProblemSet;
import org.mapstruct.BeanMapping;
import org.mapstruct.Builder;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * MapStruct mapper for ProblemSet entity to response DTO conversions.
 * Runtime fields (problemCount, completedCount, percentage, locked) are set in the service layer.
 */
@Mapper(componentModel = "spring")
public interface ProblemSetMapper {

    @Mapping(target = "problemCount", ignore = true)
    @Mapping(target = "completedCount", ignore = true)
    @Mapping(target = "percentage", ignore = true)
    @Mapping(target = "locked", ignore = true)
    ProblemSetResponseDto toResponseDto(ProblemSet problemSet);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "items", ignore = true)
    @Mapping(target = "isActive", constant = "true")
    @Mapping(target = "displayOrder", constant = "0")
    @BeanMapping(builder = @Builder(disableBuilder = true))
    ProblemSet fromCreateDto(CreateProblemSetRequestDto dto);
}
