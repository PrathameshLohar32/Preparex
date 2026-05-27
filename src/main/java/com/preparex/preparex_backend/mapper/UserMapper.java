package com.preparex.preparex_backend.mapper;

import com.preparex.preparex_backend.dto.response.UserSummaryDto;
import com.preparex.preparex_backend.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * MapStruct mapper for converting User entity to response DTOs.
 */
@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(source = "id", target = "userId")
    UserSummaryDto toUserSummaryDto(User user);
}
