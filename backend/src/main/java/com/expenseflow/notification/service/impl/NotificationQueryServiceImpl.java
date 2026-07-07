package com.expenseflow.notification.service.impl;

import com.expenseflow.notification.domain.repository.NotificationRepository;
import com.expenseflow.notification.dto.NotificationResponse;
import com.expenseflow.notification.dto.UnreadCountResponse;
import com.expenseflow.notification.mapper.NotificationMapper;
import com.expenseflow.notification.service.NotificationQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Implementation of {@link NotificationQueryService}.
 * All methods are read-only — no mutations are performed here.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NotificationQueryServiceImpl implements NotificationQueryService {

    private final NotificationRepository notificationRepository;
    private final NotificationMapper notificationMapper;

    @Override
    public Page<NotificationResponse> getNotifications(String userId, Pageable pageable) {
        return notificationRepository
                .findByUserIdOrderByCreatedAtDesc(userId, pageable)
                .map(notificationMapper::toResponse);
    }

    @Override
    public UnreadCountResponse getUnreadCount(String userId) {
        long count = notificationRepository.countUnreadByUserId(userId);
        return new UnreadCountResponse(count);
    }

    @Override
    public List<NotificationResponse> getLatestNotifications(String userId, int limit) {
        return notificationRepository
                .findByUserIdOrderByCreatedAtDesc(userId, PageRequest.of(0, limit))
                .getContent()
                .stream()
                .map(notificationMapper::toResponse)
                .toList();
    }
}
