package com.expenseflow.notification.service.impl;

import com.expenseflow.entity.UserEntity;
import com.expenseflow.notification.domain.entity.NotificationEntity;
import com.expenseflow.notification.domain.repository.NotificationRepository;
import com.expenseflow.notification.domain.valueobject.*;
import com.expenseflow.notification.dto.NotificationResponse;
import com.expenseflow.notification.dto.UnreadCountResponse;
import com.expenseflow.notification.mapper.NotificationMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("NotificationQueryServiceImpl unit tests")
class NotificationQueryServiceImplTest {

    @Mock private NotificationRepository notificationRepository;
    @Mock private NotificationMapper notificationMapper;

    @InjectMocks
    private NotificationQueryServiceImpl queryService;

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

    @Test
    @DisplayName("getNotifications: should return paginated list of notification responses")
    void getNotifications_shouldReturnPaginatedResponses() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<NotificationEntity> page = new PageImpl<>(List.of(notification));
        
        when(notificationRepository.findByUserIdOrderByCreatedAtDesc("user-123", pageable))
                .thenReturn(page);
        
        NotificationResponse response = new NotificationResponse(
                notification.getId(), "Test Title", "Test Message",
                NotificationStatus.UNREAD, NotificationPriority.NORMAL,
                NotificationCategory.SYSTEM, NotificationType.EXPENSE_POSTED,
                null, null, null, LocalDateTime.now()
        );
        when(notificationMapper.toResponse(notification)).thenReturn(response);

        Page<NotificationResponse> result = queryService.getNotifications("user-123", pageable);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).title()).isEqualTo("Test Title");
        verify(notificationRepository).findByUserIdOrderByCreatedAtDesc("user-123", pageable);
    }

    @Test
    @DisplayName("getUnreadCount: should return correct unread count")
    void getUnreadCount_shouldReturnCorrectCount() {
        when(notificationRepository.countUnreadByUserId("user-123")).thenReturn(5L);

        UnreadCountResponse response = queryService.getUnreadCount("user-123");

        assertThat(response.unreadCount()).isEqualTo(5L);
        verify(notificationRepository).countUnreadByUserId("user-123");
    }

    @Test
    @DisplayName("getLatestNotifications: should return latest list of notification responses")
    void getLatestNotifications_shouldReturnLatestResponses() {
        PageRequest pageRequest = PageRequest.of(0, 5);
        Page<NotificationEntity> page = new PageImpl<>(List.of(notification));
        
        when(notificationRepository.findByUserIdOrderByCreatedAtDesc("user-123", pageRequest))
                .thenReturn(page);
        
        NotificationResponse response = new NotificationResponse(
                notification.getId(), "Test Title", "Test Message",
                NotificationStatus.UNREAD, NotificationPriority.NORMAL,
                NotificationCategory.SYSTEM, NotificationType.EXPENSE_POSTED,
                null, null, null, LocalDateTime.now()
        );
        when(notificationMapper.toResponse(notification)).thenReturn(response);

        List<NotificationResponse> result = queryService.getLatestNotifications("user-123", 5);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).title()).isEqualTo("Test Title");
        verify(notificationRepository).findByUserIdOrderByCreatedAtDesc("user-123", pageRequest);
    }
}
