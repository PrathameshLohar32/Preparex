package com.preparex.preparex_backend.mapper;

import com.preparex.preparex_backend.dto.response.SessionInfoDto;
import com.preparex.preparex_backend.redis.model.ActiveSessionData;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * MapStruct mapper for converting Redis session data to response DTOs.
 */
@Mapper(componentModel = "spring")
public interface SessionMapper {

    @Mapping(source = "createdAt", target = "loggedInAt")
    @Mapping(target = "current", ignore = true)
    SessionInfoDto toSessionInfoDto(ActiveSessionData sessionData);
}
