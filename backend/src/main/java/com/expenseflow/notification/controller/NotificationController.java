package com.expenseflow.notification.controller;

import com.expenseflow.dto.ApiResponse;
import com.expenseflow.notification.dto.NotificationDto;
import com.expenseflow.notification.service.NotificationService;
import com.expenseflow.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<NotificationDto>>> getNotifications(
            @AuthenticationPrincipal UserPrincipal principal) {
        List<NotificationDto> notifications = notificationService.getNotifications(principal.getId());
        return ResponseEntity.ok(ApiResponse.success("Notifications retrieved successfully", notifications));
    }

    @GetMapping("/unread-count")
    public ResponseEntity<ApiResponse<Long>> getUnreadCount(
            @AuthenticationPrincipal UserPrincipal principal) {
        long count = notificationService.getUnreadCount(principal.getId());
        return ResponseEntity.ok(ApiResponse.success("Unread count retrieved successfully", count));
    }

    @PutMapping("/{id}/read")
    public ResponseEntity<ApiResponse<NotificationDto>> markAsRead(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable("id") String id) {
        NotificationDto notification = notificationService.markAsRead(principal.getId(), id);
        return ResponseEntity.ok(ApiResponse.success("Notification marked as read successfully", notification));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> archiveNotification(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable("id") String id) {
        notificationService.archiveNotification(principal.getId(), id);
        return ResponseEntity.ok(ApiResponse.success("Notification archived successfully", null));
    }
}
