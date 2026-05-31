package com.preparex.preparex_backend.mapper;

import com.preparex.preparex_backend.dto.response.TopicResponseDto;
import com.preparex.preparex_backend.entity.Topic;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

/**
 * MapStruct mapper for Topic entity to response DTO conversions.
 * Denormalizes subject name and ID for frontend convenience.
 */
@Mapper(componentModel = "spring")
public interface TopicMapper {

    @Mapping(source = "subject.id", target = "subjectId")
    @Mapping(source = "subject.name", target = "subjectName")
    TopicResponseDto toResponseDto(Topic topic);

    List<TopicResponseDto> toResponseDtoList(List<Topic> topics);
}
