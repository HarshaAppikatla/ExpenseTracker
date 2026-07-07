package com.expenseflow.notification.domain.repository;

import com.expenseflow.notification.domain.entity.NotificationEntity;
import com.expenseflow.notification.domain.valueobject.NotificationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for the Notification aggregate root.
 * All queries exclude soft-deleted records unless explicitly stated.
 */
@Repository
public interface NotificationRepository extends JpaRepository<NotificationEntity, String>,
        JpaSpecificationExecutor<NotificationEntity> {

    // -------------------------------------------------------------------------
    // Query — paginated list (used by GET /notifications)
    // -------------------------------------------------------------------------

    @Query("""
            SELECT n FROM NotificationEntity n
            WHERE n.user.id = :userId AND n.isDeleted = false
            ORDER BY n.createdAt DESC
            """)
    Page<NotificationEntity> findByUserIdOrderByCreatedAtDesc(
            @Param("userId") String userId, Pageable pageable);

    // -------------------------------------------------------------------------
    // Query — unread list (used by markAllAsRead)
    // -------------------------------------------------------------------------

    @Query("""
            SELECT n FROM NotificationEntity n
            WHERE n.user.id = :userId
              AND n.status = com.expenseflow.notification.domain.valueobject.NotificationStatus.UNREAD
              AND n.isDeleted = false
            """)
    List<NotificationEntity> findUnreadByUserId(@Param("userId") String userId);

    // -------------------------------------------------------------------------
    // Query — unread count (used by notification bell)
    // -------------------------------------------------------------------------

    @Query("""
            SELECT COUNT(n) FROM NotificationEntity n
            WHERE n.user.id = :userId
              AND n.status = com.expenseflow.notification.domain.valueobject.NotificationStatus.UNREAD
              AND n.isDeleted = false
            """)
    long countUnreadByUserId(@Param("userId") String userId);

    // -------------------------------------------------------------------------
    // Query — status-filtered page (optional, kept for future use)
    // -------------------------------------------------------------------------

    @Query("""
            SELECT n FROM NotificationEntity n
            WHERE n.user.id = :userId AND n.status = :status AND n.isDeleted = false
            ORDER BY n.createdAt DESC
            """)
    Page<NotificationEntity> findByUserIdAndStatusOrderByCreatedAtDesc(
            @Param("userId") String userId,
            @Param("status") NotificationStatus status,
            Pageable pageable);
}
