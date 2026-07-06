package com.expenseflow.notification.service;

import com.expenseflow.notification.dto.NotificationDto;
import com.expenseflow.notification.domain.valueobject.NotificationPriority;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class NotificationService {

    @Transactional(readOnly = true)
    public List<NotificationDto> getNotifications(String userId) {
        return Collections.emptyList();
    }

    @Transactional(readOnly = true)
    public long getUnreadCount(String userId) {
        return 0;
    }

    public NotificationDto markAsRead(String userId, String notificationId) {
        return null;
    }

    public void archiveNotification(String userId, String notificationId) {
        log.info("Stub archive notification: {} by {}", notificationId, userId);
    }

    public void createNotification(String userId, String type, String title, String message, NotificationPriority priority) {
        log.info("Stub create notification for user {}: {}", userId, title);
    }
}
