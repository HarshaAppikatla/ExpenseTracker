package com.expenseflow.notification.service.impl;

import com.expenseflow.entity.UserEntity;
import com.expenseflow.notification.domain.entity.NotificationEntity;
import com.expenseflow.notification.domain.repository.NotificationRepository;
import com.expenseflow.notification.domain.valueobject.*;
import com.expenseflow.notification.dto.NotificationResponse;
import com.expenseflow.notification.exception.NotificationNotFoundException;
import com.expenseflow.notification.exception.NotificationPermissionDeniedException;
import com.expenseflow.notification.mapper.NotificationMapper;
import com.expenseflow.repository.UserRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("NotificationCommandServiceImpl unit tests")
class NotificationCommandServiceImplTest {

    @Mock private NotificationRepository notificationRepository;
    @Mock private UserRepository userRepository;
    @Mock private NotificationMapper notificationMapper;
    @Mock private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private NotificationCommandServiceImpl commandService;

    private UserEntity user;
    private NotificationEntity notification;

    @BeforeEach
    void setUp() {
        user = new UserEntity();
        user.setId("user-123");

        notification = NotificationEntity.create(
                user,
                "Test Title",
                "Test Message",
                NotificationPriority.NORMAL,
                NotificationCategory.SYSTEM,
                NotificationType.EXPENSE_POSTED,
                null
        );
    }

    // =========================================================================
    // createNotification
    // =========================================================================

    @Test
    @DisplayName("createNotification: should persist notification when user exists")
    void createNotification_whenUserExists_shouldSave() {
        when(userRepository.findById("user-123")).thenReturn(Optional.of(user));
        NotificationResponse mockResponse = new NotificationResponse(
                "id-123", "Title", "Message", NotificationStatus.UNREAD,
                NotificationPriority.NORMAL, NotificationCategory.EXPENSE,
                NotificationType.EXPENSE_POSTED, null, null, null, LocalDateTime.now()
        );
        when(notificationMapper.toResponse(any())).thenReturn(mockResponse);

        commandService.createNotification(
                "user-123",
                NotificationType.EXPENSE_POSTED,
                NotificationCategory.EXPENSE,
                NotificationPriority.NORMAL,
                "Title",
                "Message",
                null
        );

        verify(notificationRepository, times(1)).save(any(NotificationEntity.class));
        verify(eventPublisher, times(1)).publishEvent(any());
    }

    @Test
    @DisplayName("createNotification: should skip and log when user does not exist")
    void createNotification_whenUserNotFound_shouldSkip() {
        when(userRepository.findById("unknown")).thenReturn(Optional.empty());

        commandService.createNotification(
                "unknown",
                NotificationType.EXPENSE_POSTED,
                NotificationCategory.EXPENSE,
                NotificationPriority.NORMAL,
                "Title",
                "Message",
                null
        );

        verify(notificationRepository, never()).save(any());
    }

    // =========================================================================
    // markAsRead
    // =========================================================================

    @Test
    @DisplayName("markAsRead: should transition status to READ and return response")
    void markAsRead_shouldTransitionToRead() {
        NotificationResponse mockResponse = new NotificationResponse(
                notification.getId(), "Test Title", "Test Message",
                NotificationStatus.READ, NotificationPriority.NORMAL,
                NotificationCategory.SYSTEM, NotificationType.EXPENSE_POSTED,
                null, null, null, LocalDateTime.now()
        );

        when(notificationRepository.findById(notification.getId()))
                .thenReturn(Optional.of(notification));
        when(notificationMapper.toResponse(notification)).thenReturn(mockResponse);

        NotificationResponse result = commandService.markAsRead("user-123", notification.getId());

        assertThat(result.status()).isEqualTo(NotificationStatus.READ);
        verify(notificationRepository).save(notification);
    }

    @Test
    @DisplayName("markAsRead: should throw NotificationNotFoundException when not found")
    void markAsRead_whenNotFound_shouldThrow() {
        when(notificationRepository.findById("ghost-id")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> commandService.markAsRead("user-123", "ghost-id"))
                .isInstanceOf(NotificationNotFoundException.class)
                .hasMessageContaining("ghost-id");
    }

    @Test
    @DisplayName("markAsRead: should throw NotificationPermissionDeniedException when ownership mismatch")
    void markAsRead_whenOwnershipMismatch_shouldThrow() {
        when(notificationRepository.findById(notification.getId()))
                .thenReturn(Optional.of(notification));

        assertThatThrownBy(() -> commandService.markAsRead("other-user", notification.getId()))
                .isInstanceOf(NotificationPermissionDeniedException.class);
    }

    // =========================================================================
    // markAllAsRead
    // =========================================================================

    @Test
    @DisplayName("markAllAsRead: should mark all unread notifications and return count")
    void markAllAsRead_shouldReturnUpdatedCount() {
        NotificationEntity n2 = NotificationEntity.create(
                user, "T2", "M2", NotificationPriority.LOW,
                NotificationCategory.SYSTEM, NotificationType.USER_ADDED, null);

        when(notificationRepository.findUnreadByUserId("user-123"))
                .thenReturn(List.of(notification, n2));

        int count = commandService.markAllAsRead("user-123");

        assertThat(count).isEqualTo(2);
        verify(notificationRepository).saveAll(any());
    }

    // =========================================================================
    // archiveNotification
    // =========================================================================

    @Test
    @DisplayName("archiveNotification: should soft-delete notification")
    void archiveNotification_shouldSetIsDeletedTrue() {
        when(notificationRepository.findById(notification.getId()))
                .thenReturn(Optional.of(notification));

        commandService.archiveNotification("user-123", notification.getId());

        assertThat(notification.isDeleted()).isTrue();
        verify(notificationRepository).save(notification);
    }
}
