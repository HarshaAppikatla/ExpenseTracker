package com.expenseflow.group.service;

import com.expenseflow.group.entity.GroupCode;
import com.expenseflow.group.entity.GroupEntity;
import com.expenseflow.group.exception.InvalidRoomCodeException;
import com.expenseflow.group.exception.RoomCodeCollisionException;
import com.expenseflow.group.repository.GroupRepository;
import com.expenseflow.group.service.impl.GroupAccessServiceImpl;
import com.expenseflow.group.validation.RoomCodeValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import java.util.Optional;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GroupAccessServiceTest {

    @Mock
    private GroupRepository groupRepository;

    @Mock
    private RoomCodeValidator roomCodeValidator;

    @InjectMocks
    private GroupAccessServiceImpl groupAccessService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(groupAccessService, "baseUrl", "http://localhost:8080/join/");
    }

    @Test
    void generateGroupCode_ShouldGenerateValidCode() {
        when(groupRepository.existsByGroupCodeAndIsDeletedFalse(any(GroupCode.class))).thenReturn(false);

        GroupCode code = groupAccessService.generateGroupCode();

        assertThat(code).isNotNull();
        assertThat(code.value()).hasSize(8);
        assertThat(code.value()).matches("^[A-Z2-9]+$");
        // No invalid characters like I, O, 0, 1
        assertThat(code.value()).doesNotContain("I", "O", "0", "1");
    }

    @Test
    void generateGroupCode_ShouldRetryOnCollisionAndSucceed() {
        when(groupRepository.existsByGroupCodeAndIsDeletedFalse(any(GroupCode.class)))
                .thenReturn(true) // First attempt collision
                .thenReturn(true) // Second attempt collision
                .thenReturn(false); // Third attempt unique

        GroupCode code = groupAccessService.generateGroupCode();

        assertThat(code).isNotNull();
        verify(groupRepository, times(3)).existsByGroupCodeAndIsDeletedFalse(any(GroupCode.class));
    }

    @Test
    void generateGroupCode_ShouldThrowExceptionWhenCollisionRetriesExhausted() {
        when(groupRepository.existsByGroupCodeAndIsDeletedFalse(any(GroupCode.class))).thenReturn(true);

        assertThatThrownBy(() -> groupAccessService.generateGroupCode())
                .isInstanceOf(RoomCodeCollisionException.class)
                .hasMessageContaining("Failed to generate a unique room code after 5 attempts");

        verify(groupRepository, times(5)).existsByGroupCodeAndIsDeletedFalse(any(GroupCode.class));
    }

    @Test
    void validateAndResolveCode_ShouldReturnGroupWhenValid() {
        String validCodeStr = "ABCDEFGH";
        GroupCode code = new GroupCode(validCodeStr);
        GroupEntity group = new GroupEntity();

        when(groupRepository.findByGroupCodeAndIsDeletedFalse(code)).thenReturn(Optional.of(group));

        GroupEntity resolved = groupAccessService.validateAndResolveCode(validCodeStr);

        assertThat(resolved).isSameAs(group);
        verify(roomCodeValidator).validateRoomCodeActive(group);
    }

    @Test
    void validateAndResolveCode_ShouldThrowExceptionWhenCodeNotFound() {
        String codeStr = "ABCDEFGH";
        GroupCode code = new GroupCode(codeStr);

        when(groupRepository.findByGroupCodeAndIsDeletedFalse(code)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> groupAccessService.validateAndResolveCode(codeStr))
                .isInstanceOf(InvalidRoomCodeException.class)
                .hasMessageContaining("Invalid room code");
    }

    @Test
    void validateAndResolveCode_ShouldThrowExceptionWhenCodeIsInvalidFormat() {
        String invalidCodeStr = "abc"; // lowercase and short

        assertThatThrownBy(() -> groupAccessService.validateAndResolveCode(invalidCodeStr))
                .isInstanceOf(IllegalArgumentException.class); // Thrown by GroupCode constructor validation
    }

    @Test
    void generateJoinLink_ShouldFormatUrlCorrectly() {
        GroupCode code = new GroupCode("XYZ12345");
        String joinLink = groupAccessService.generateJoinLink(code);

        assertThat(joinLink).isEqualTo("http://localhost:8080/join/XYZ12345");
    }

    @Test
    void generateJoinLink_ShouldReturnNullWhenCodeIsNull() {
        String joinLink = groupAccessService.generateJoinLink(null);
        assertThat(joinLink).isNull();
    }
}
