package org.zhan.recipe_backend.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.zhan.recipe_backend.dto.UserDto;
import org.zhan.recipe_backend.entity.User;

@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(target = "createdCount", ignore = true)
    @Mapping(target = "savedCount", ignore = true)
    UserDto  toUserDto(User user);
}
