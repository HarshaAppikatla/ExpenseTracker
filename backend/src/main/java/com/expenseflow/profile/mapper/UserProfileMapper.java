package com.expenseflow.profile.mapper;

import com.expenseflow.profile.dto.UserProfileResponse;
import com.expenseflow.profile.entity.UserProfileEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserProfileMapper {

    @Mapping(target = "userId", source = "user.id")
    UserProfileResponse toResponse(UserProfileEntity entity);
}
