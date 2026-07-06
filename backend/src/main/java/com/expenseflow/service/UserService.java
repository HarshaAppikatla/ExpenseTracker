package com.expenseflow.service;

import com.expenseflow.dto.user.UserDto;
import com.expenseflow.entity.UserEntity;

public interface UserService {
    UserDto getCurrentUserProfile(String email);
    UserEntity findUserEntityByEmail(String email);
    boolean existsByEmail(String email);
    UserDto getUserById(String id);
    java.util.List<UserDto> getUsersByIds(java.util.List<String> ids);
}
