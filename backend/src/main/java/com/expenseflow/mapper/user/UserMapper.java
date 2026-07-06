package com.expenseflow.mapper.user;

import com.expenseflow.dto.user.UserDto;
import com.expenseflow.entity.UserEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(target = "roles", expression = "java(user.getRoles() == null ? null : user.getRoles().stream().map(role -> role.getName()).collect(java.util.stream.Collectors.toSet()))")
    UserDto toDto(UserEntity user);
}
