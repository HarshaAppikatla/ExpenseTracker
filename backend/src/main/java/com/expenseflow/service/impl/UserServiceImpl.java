package com.expenseflow.service.impl;

import com.expenseflow.dto.user.UserDto;
import com.expenseflow.entity.UserEntity;
import com.expenseflow.mapper.user.UserMapper;
import com.expenseflow.repository.UserRepository;
import com.expenseflow.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Override
    @Transactional(readOnly = true)
    public UserDto getCurrentUserProfile(String email) {
        UserEntity user = findUserEntityByEmail(email);
        return userMapper.toDto(user);
    }

    @Override
    @Transactional(readOnly = true)
    public UserEntity findUserEntityByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + email));
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    @Override
    @Transactional(readOnly = true)
    public UserDto getUserById(String id) {
        return userRepository.findById(id)
                .map(userMapper::toDto)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public java.util.List<UserDto> getUsersByIds(java.util.List<String> ids) {
        return userRepository.findAllById(ids).stream()
                .map(userMapper::toDto)
                .toList();
    }
}
