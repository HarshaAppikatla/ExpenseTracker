package com.expenseflow.group.mapper;

import com.expenseflow.group.dto.*;
import com.expenseflow.group.entity.*;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface GroupMapper {

    @Mapping(target = "groupCode", source = "entity.groupCode.value")
    @Mapping(target = "isOwner", expression = "java(entity.getOwner() != null && currentUserId != null && currentUserId.equals(entity.getOwner().getId()))")
    @Mapping(target = "role", source = "userRole")
    @Mapping(target = "settings", source = "entity.settings")
    GroupDto toDto(GroupEntity entity, String currentUserId, String userRole);

    GroupSettingsDto toSettingsDto(GroupSettings settings);

    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "name", source = "user.fullName")
    @Mapping(target = "email", source = "user.email")
    @Mapping(target = "role", expression = "java(entity.getRole().name())")
    GroupMemberDto toMemberDto(GroupMemberEntity entity);

    @Mapping(target = "actionType", expression = "java(entity.getActionType().name())")
    GroupActivityDto toActivityDto(GroupActivityEntity entity);
}
