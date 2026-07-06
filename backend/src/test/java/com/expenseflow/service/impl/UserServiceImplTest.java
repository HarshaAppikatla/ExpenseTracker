package com.expenseflow.service.impl;

import com.expenseflow.dto.user.UserDto;
import com.expenseflow.entity.UserEntity;
import com.expenseflow.mapper.user.UserMapper;
import com.expenseflow.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private UserServiceImpl userService;

    @Test
    void testGetCurrentUserProfile_Success() {
        String email = "test@example.com";
        UserEntity userEntity = new UserEntity();
        userEntity.setEmail(email);

        UserDto userDto = new UserDto();
        userDto.setEmail(email);

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(userEntity));
        when(userMapper.toDto(userEntity)).thenReturn(userDto);

        UserDto result = userService.getCurrentUserProfile(email);

        assertThat(result).isNotNull();
        assertThat(result.getEmail()).isEqualTo(email);
        verify(userRepository).findByEmail(email);
        verify(userMapper).toDto(userEntity);
    }

    @Test
    void testGetCurrentUserProfile_NotFound_ThrowsException() {
        String email = "missing@example.com";
        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getCurrentUserProfile(email))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("User not found");
    }

    @Test
    void testExistsByEmail_ReturnsTrue() {
        String email = "exists@example.com";
        when(userRepository.existsByEmail(email)).thenReturn(true);

        boolean result = userService.existsByEmail(email);

        assertThat(result).isTrue();
        verify(userRepository).existsByEmail(email);
    }

    @Test
    void testExistsByEmail_ReturnsFalse() {
        String email = "missing@example.com";
        when(userRepository.existsByEmail(email)).thenReturn(false);

        boolean result = userService.existsByEmail(email);

        assertThat(result).isFalse();
        verify(userRepository).existsByEmail(email);
    }
}
