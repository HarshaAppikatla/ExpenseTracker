package com.expenseflow.notification.domain.entity;

import com.expenseflow.entity.BaseEntity;
import com.expenseflow.entity.UserEntity;
import com.expenseflow.notification.domain.valueobject.*;
import jakarta.persistence.*;
import lombok.*;

import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "notifications")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class NotificationEntity extends BaseEntity {

    @Id
    @Column(name = "id", length = 36, nullable = false, updatable = false)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, updatable = false)
    private UserEntity user;

    @Column(name = "title", nullable = false, length = 255, updatable = false)
    private String title;

    @Column(name = "message", nullable = false, length = 500, updatable = false)
    private String message;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private NotificationStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "priority", nullable = false, length = 20, updatable = false)
    private NotificationPriority priority;

    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false, length = 20, updatable = false)
    private NotificationCategory category;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 50, updatable = false)
    private NotificationType type;

    @Convert(converter = NotificationPayloadConverter.class)
    @Column(name = "payload", columnDefinition = "TEXT", updatable = false)
    private NotificationPayload payload;

    /**
     * Factory constructor for creating new notifications.
     */
    public static NotificationEntity create(
            UserEntity user,
            String title,
            String message,
            NotificationPriority priority,
            NotificationCategory category,
            NotificationType type,
            NotificationPayload payload) {
        
        Objects.requireNonNull(user, "User recipient cannot be null");
        Objects.requireNonNull(title, "Title cannot be null");
        Objects.requireNonNull(message, "Message cannot be null");
        Objects.requireNonNull(priority, "Priority cannot be null");
        Objects.requireNonNull(category, "Category cannot be null");
        Objects.requireNonNull(type, "Type cannot be null");

        NotificationEntity notification = new NotificationEntity();
        notification.id = UUID.randomUUID().toString();
        notification.user = user;
        notification.title = title;
        notification.message = message;
        notification.status = NotificationStatus.UNREAD;
        notification.priority = priority;
        notification.category = category;
        notification.type = type;
        notification.payload = payload;
        
        return notification;
    }

    /**
     * Transitions notification status to READ.
     */
    public void markAsRead() {
        if (this.status == NotificationStatus.UNREAD) {
            this.status = NotificationStatus.READ;
        }
    }

    /**
     * Transitions notification status to UNREAD.
     */
    public void markAsUnread() {
        if (this.status == NotificationStatus.READ) {
            this.status = NotificationStatus.UNREAD;
        }
    }
}
