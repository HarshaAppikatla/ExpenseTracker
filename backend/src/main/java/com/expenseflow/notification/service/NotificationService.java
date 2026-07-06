package com.expenseflow.notification.service;

import com.expenseflow.entity.UserEntity;
import com.expenseflow.exception.SecurityHardeningException;
import com.expenseflow.notification.dto.NotificationDto;
import com.expenseflow.notification.entity.NotificationEntity;
import com.expenseflow.notification.entity.NotificationPriority;
import com.expenseflow.notification.entity.NotificationStatus;
import com.expenseflow.notification.repository.NotificationRepository;
import com.expenseflow.repository.UserRepository;
import com.expenseflow.core.event.NotificationArchivedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional(readOnly = true)
    public List<NotificationDto> getNotifications(String userId) {
        return notificationRepository.findByUserId(userId).stream()
                .filter(n -> n.getStatus() != NotificationStatus.ARCHIVED)
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public long getUnreadCount(String userId) {
        return notificationRepository.countByUserIdAndStatus(userId, NotificationStatus.UNREAD);
    }

    public NotificationDto markAsRead(String userId, String notificationId) {
        NotificationEntity notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new SecurityHardeningException("Notification not found.", "NOT_001"));

        if (!notification.getUser().getId().equals(userId)) {
            throw new SecurityHardeningException("Access denied. You do not own this notification.", "NOT_002");
        }

        notification.setStatus(NotificationStatus.READ);
        NotificationEntity saved = notificationRepository.save(notification);
        return toDto(saved);
    }

    public void archiveNotification(String userId, String notificationId) {
        NotificationEntity notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new SecurityHardeningException("Notification not found.", "NOT_001"));

        if (!notification.getUser().getId().equals(userId)) {
            throw new SecurityHardeningException("Access denied. You do not own this notification.", "NOT_002");
        }

        notification.setStatus(NotificationStatus.ARCHIVED);
        notificationRepository.save(notification);

        eventPublisher.publishEvent(new NotificationArchivedEvent(this, notificationId, userId));
        log.info("Notification {} archived by user {}", notificationId, userId);
    }

    public void createNotification(String userId, String type, String title, String message, NotificationPriority priority) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new SecurityHardeningException("User not found.", "USR_001"));

        NotificationEntity notification = NotificationEntity.builder()
                .id(UUID.randomUUID().toString())
                .user(user)
                .notificationType(type)
                .title(title)
                .message(message)
                .status(NotificationStatus.UNREAD)
                .priority(priority)
                .createdAt(LocalDateTime.now())
                .build();

        notificationRepository.save(notification);
        log.info("Notification created for user {}: {}", userId, title);
    }

    private NotificationDto toDto(NotificationEntity entity) {
        return new NotificationDto(
                entity.getId(),
                entity.getUser().getId(),
                entity.getNotificationType(),
                entity.getTitle(),
                entity.getMessage(),
                entity.getStatus(),
                entity.getPriority(),
                entity.getCreatedAt()
        );
    }
}
