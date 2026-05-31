package com.preparex.preparex_backend.mapper;

import com.preparex.preparex_backend.dto.response.SubjectResponseDto;
import com.preparex.preparex_backend.entity.Subject;
import org.mapstruct.Mapper;

import java.util.List;

/**
 * MapStruct mapper for Subject entity to response DTO conversions.
 */
@Mapper(componentModel = "spring")
public interface SubjectMapper {

    SubjectResponseDto toResponseDto(Subject subject);

    List<SubjectResponseDto> toResponseDtoList(List<Subject> subjects);
}
