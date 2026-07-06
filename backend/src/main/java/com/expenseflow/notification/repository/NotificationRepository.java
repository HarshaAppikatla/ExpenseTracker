package com.expenseflow.notification.repository;

import com.expenseflow.notification.entity.NotificationEntity;
import com.expenseflow.notification.entity.NotificationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<NotificationEntity, String>, JpaSpecificationExecutor<NotificationEntity> {
    List<NotificationEntity> findByUserIdAndStatus(String userId, NotificationStatus status);
    List<NotificationEntity> findByUserId(String userId);
    long countByUserIdAndStatus(String userId, NotificationStatus status);
}
