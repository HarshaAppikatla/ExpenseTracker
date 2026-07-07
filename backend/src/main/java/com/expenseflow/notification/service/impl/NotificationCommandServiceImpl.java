package com.expenseflow.notification.service.impl;

import com.expenseflow.entity.UserEntity;
import com.expenseflow.notification.domain.entity.NotificationEntity;
import com.expenseflow.notification.domain.event.NotificationCreatedEvent;
import com.expenseflow.notification.domain.repository.NotificationRepository;
import com.expenseflow.notification.domain.valueobject.*;
import com.expenseflow.notification.dto.NotificationResponse;
import com.expenseflow.notification.exception.NotificationNotFoundException;
import com.expenseflow.notification.exception.NotificationPermissionDeniedException;
import com.expenseflow.notification.mapper.NotificationMapper;
import com.expenseflow.notification.service.NotificationCommandService;
import com.expenseflow.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Implementation of {@link NotificationCommandService}.
 *
 * <p>Architectural constraints (ADR-006):
 * <ul>
 *   <li>Only the service layer creates/mutates notifications — not the repository</li>
 *   <li>This context must NOT import from Expense, Settlement, Group, or Trip contexts</li>
 *   <li>Repository methods must not publish domain events</li>
 * </ul>
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class NotificationCommandServiceImpl implements NotificationCommandService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final NotificationMapper notificationMapper;
    private final ApplicationEventPublisher eventPublisher;

    // =========================================================================
    // Create
    // =========================================================================

    @Override
    public void createNotification(
            String userId,
            NotificationType type,
            NotificationCategory category,
            NotificationPriority priority,
            String title,
            String message,
            NotificationPayload payload) {

        UserEntity user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            log.warn("Notification skipped — user not found: {}", userId);
            return;
        }

        NotificationEntity notification = NotificationEntity.create(
                user, title, message, priority, category, type, payload);

        notificationRepository.save(notification);
        log.debug("Notification created [type={}, user={}]: {}", type, userId, title);

        NotificationResponse response = notificationMapper.toResponse(notification);
        eventPublisher.publishEvent(new NotificationCreatedEvent(this, notification.getId(), userId, response));
    }

    @Override
    @Deprecated(forRemoval = true)
    public void createNotification(String userId, String type, String title, String message, NotificationPriority priority) {
        NotificationCategory category = resolveCategory(type);
        NotificationType notifType    = resolveType(type);
        createNotification(userId, notifType, category, priority, title, message, null);
    }

    // =========================================================================
    // Lifecycle transitions
    // =========================================================================

    @Override
    public NotificationResponse markAsRead(String userId, String notificationId) {
        NotificationEntity notification = findAndValidateOwnership(userId, notificationId);
        notification.markAsRead();
        notificationRepository.save(notification);
        log.debug("Notification {} marked as READ by user {}", notificationId, userId);
        return notificationMapper.toResponse(notification);
    }

    @Override
    public int markAllAsRead(String userId) {
        List<NotificationEntity> unread = notificationRepository.findUnreadByUserId(userId);
        unread.forEach(NotificationEntity::markAsRead);
        notificationRepository.saveAll(unread);
        log.debug("Marked {} notifications as READ for user {}", unread.size(), userId);
        return unread.size();
    }

    @Override
    public void archiveNotification(String userId, String notificationId) {
        NotificationEntity notification = findAndValidateOwnership(userId, notificationId);
        notification.softDelete();
        notificationRepository.save(notification);
        log.debug("Notification {} archived by user {}", notificationId, userId);
    }

    // =========================================================================
    // Private helpers
    // =========================================================================

    private NotificationEntity findAndValidateOwnership(String userId, String notificationId) {
        NotificationEntity notification = notificationRepository
                .findById(notificationId)
                .orElseThrow(() -> new NotificationNotFoundException(notificationId));

        if (!notification.getUser().getId().equals(userId)) {
            throw new NotificationPermissionDeniedException(notificationId, userId);
        }
        return notification;
    }

    private static NotificationCategory resolveCategory(String type) {
        return switch (type) {
            case "BUDGET_LIMIT", "BUDGET_WARNING", "RECURRING_EXECUTION",
                 "RECURRING_FAILED", "EXPENSE_POSTED"            -> NotificationCategory.EXPENSE;
            case "SETTLEMENT_GENERATED", "SETTLEMENT_CONFIRMED",
                 "SETTLEMENT_DISPUTED"                           -> NotificationCategory.SETTLEMENT;
            case "USER_ADDED"                                    -> NotificationCategory.GROUP;
            default                                              -> NotificationCategory.SYSTEM;
        };
    }

    private static NotificationType resolveType(String type) {
        return switch (type) {
            case "EXPENSE_POSTED"                                     -> NotificationType.EXPENSE_POSTED;
            case "SETTLEMENT_GENERATED"                               -> NotificationType.SETTLEMENT_GENERATED;
            case "SETTLEMENT_CONFIRMED"                               -> NotificationType.SETTLEMENT_CONFIRMED;
            case "SETTLEMENT_DISPUTED"                                -> NotificationType.SETTLEMENT_DISPUTED;
            case "USER_ADDED"                                         -> NotificationType.USER_ADDED;
            case "BUDGET_LIMIT", "BUDGET_LIMIT_EXCEEDED"             -> NotificationType.BUDGET_LIMIT_EXCEEDED;
            case "BUDGET_WARNING"                                     -> NotificationType.BUDGET_WARNING;
            case "RECURRING_EXECUTION"                                -> NotificationType.RECURRING_EXECUTION;
            case "RECURRING_FAILED", "RECURRING_EXECUTION_FAILED"     -> NotificationType.RECURRING_EXECUTION_FAILED;
            case "SAVINGS_COMPLETED", "SAVINGS_GOAL_COMPLETED"        -> NotificationType.SAVINGS_GOAL_COMPLETED;
            default                                                   -> NotificationType.EXPENSE_POSTED;
        };
    }
}
