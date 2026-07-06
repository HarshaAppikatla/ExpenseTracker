package com.expenseflow.notification.domain.repository;

import com.expenseflow.notification.domain.entity.NotificationEntity;
import com.expenseflow.notification.domain.valueobject.NotificationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface NotificationRepository extends JpaRepository<NotificationEntity, String>, JpaSpecificationExecutor<NotificationEntity> {

    Optional<NotificationEntity> findByIdAndIsDeletedFalse(String id);

    Page<NotificationEntity> findByUserIdAndIsDeletedFalse(String userId, Pageable pageable);

    Page<NotificationEntity> findByUserIdAndStatusAndIsDeletedFalse(String userId, NotificationStatus status, Pageable pageable);

    long countByUserIdAndStatusAndIsDeletedFalse(String userId, NotificationStatus status);

    @Query("SELECT n FROM NotificationEntity n WHERE n.user.id = :userId AND n.isDeleted = false ORDER BY n.createdAt DESC")
    List<NotificationEntity> findLatestNotifications(@Param("userId") String userId, Pageable pageable);

    @Modifying
    @Query("UPDATE NotificationEntity n SET n.status = 'READ' WHERE n.user.id = :userId AND n.status = 'UNREAD' AND n.isDeleted = false")
    int markAllAsRead(@Param("userId") String userId);
}
