package com.expenseflow.notification.entity;

import com.expenseflow.entity.UserEntity;
import com.expenseflow.notification.domain.entity.NotificationEntity;
import com.expenseflow.notification.domain.valueobject.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class NotificationEntityTest {

    @Test
    @DisplayName("Should successfully create a notification aggregate in UNREAD status with immutable properties")
    void testCreateNotification_Success() {
        UserEntity user = new UserEntity();
        user.setId("user-1");

        NotificationPayload payload = new NotificationPayload(
                "expense-1", "group-1", null, new BigDecimal("150.00"), "INR", 1
        );

        NotificationEntity notification = NotificationEntity.create(
                user, "New Expense", "Alice posted an expense of ₹150.00",
                NotificationPriority.NORMAL, NotificationCategory.EXPENSE,
                NotificationType.EXPENSE_POSTED, payload
        );

        assertThat(notification.getId()).isNotNull();
        assertThat(notification.getUser()).isEqualTo(user);
        assertThat(notification.getTitle()).isEqualTo("New Expense");
        assertThat(notification.getStatus()).isEqualTo(NotificationStatus.UNREAD);
        assertThat(notification.getPriority()).isEqualTo(NotificationPriority.NORMAL);
        assertThat(notification.getCategory()).isEqualTo(NotificationCategory.EXPENSE);
        assertThat(notification.getType()).isEqualTo(NotificationType.EXPENSE_POSTED);
        assertThat(notification.getPayload()).isEqualTo(payload);
    }

    @Test
    @DisplayName("Should throw NullPointerException when required parameters are missing during creation")
    void testCreateNotification_Preconditions() {
        UserEntity user = new UserEntity();

        assertThatThrownBy(() -> NotificationEntity.create(
                null, "Title", "Msg", NotificationPriority.NORMAL,
                NotificationCategory.EXPENSE, NotificationType.EXPENSE_POSTED, null
        )).isInstanceOf(NullPointerException.class).hasMessageContaining("User recipient cannot be null");

        assertThatThrownBy(() -> NotificationEntity.create(
                user, null, "Msg", NotificationPriority.NORMAL,
                NotificationCategory.EXPENSE, NotificationType.EXPENSE_POSTED, null
        )).isInstanceOf(NullPointerException.class).hasMessageContaining("Title cannot be null");

        assertThatThrownBy(() -> NotificationEntity.create(
                user, "Title", null, NotificationPriority.NORMAL,
                NotificationCategory.EXPENSE, NotificationType.EXPENSE_POSTED, null
        )).isInstanceOf(NullPointerException.class).hasMessageContaining("Message cannot be null");
    }

    @Test
    @DisplayName("Should transition status correctly between READ and UNREAD states")
    void testNotificationLifecycleTransitions() {
        UserEntity user = new UserEntity();
        NotificationEntity notification = NotificationEntity.create(
                user, "Title", "Msg", NotificationPriority.LOW,
                NotificationCategory.SYSTEM, NotificationType.USER_ADDED, null
        );

        assertThat(notification.getStatus()).isEqualTo(NotificationStatus.UNREAD);

        notification.markAsRead();
        assertThat(notification.getStatus()).isEqualTo(NotificationStatus.READ);

        notification.markAsUnread();
        assertThat(notification.getStatus()).isEqualTo(NotificationStatus.UNREAD);
    }

    @Test
    @DisplayName("Should successfully serialize and deserialize payload using JPA Converter")
    void testNotificationPayloadConverter() {
        NotificationPayloadConverter converter = new NotificationPayloadConverter();
        NotificationPayload payload = new NotificationPayload(
                "exp-abc", "grp-xyz", "trip-123", new BigDecimal("500.25"), "USD", 1
        );

        String json = converter.convertToDatabaseColumn(payload);
        NotificationPayload deserialized = converter.convertToEntityAttribute(json);

        assertThat(json).contains("exp-abc", "grp-xyz", "trip-123", "500.25", "USD");
        assertThat(deserialized).isEqualTo(payload);
    }
}
