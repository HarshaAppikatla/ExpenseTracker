package com.expenseflow.notification.controller;

import com.expenseflow.dto.ApiResponse;
import com.expenseflow.notification.dto.NotificationResponse;
import com.expenseflow.notification.dto.UnreadCountResponse;
import com.expenseflow.notification.service.NotificationCommandService;
import com.expenseflow.notification.service.NotificationQueryService;
import com.expenseflow.security.UserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.List;

/**
 * REST controller for the Notification bounded context.
 *
 * <p>All endpoints are user-scoped. The authenticated user's ID is obtained
 * from the Spring Security context via {@link UserPrincipal} — never from
 * a request parameter (per ADR-006 security constraints).
 *
 * <p>Base path: {@code /api/v1/notifications}
 */
@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
@Tag(name = "Notifications", description = "User notification management — list, mark read, archive")
public class NotificationController {

    private final NotificationCommandService notificationCommandService;
    private final NotificationQueryService   notificationQueryService;

    // =========================================================================
    // GET /api/v1/notifications
    // =========================================================================

    @GetMapping
    @Operation(summary = "List notifications for the current user (newest first, paginated)")
    public ResponseEntity<ApiResponse<Page<NotificationResponse>>> getNotifications(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "20") int size) {

        Pageable pageable = PageRequest.of(page, Math.min(size, 100));
        Page<NotificationResponse> notifications =
                notificationQueryService.getNotifications(principal.getId(), pageable);
        return ResponseEntity.ok(ApiResponse.success("Notifications retrieved", notifications));
    }

    // =========================================================================
    // GET /api/v1/notifications/latest
    // =========================================================================

    @GetMapping("/latest")
    @Operation(summary = "Get the latest notifications for the current user")
    public ResponseEntity<ApiResponse<List<NotificationResponse>>> getLatestNotifications(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam(defaultValue = "5") int limit) {

        List<NotificationResponse> notifications =
                notificationQueryService.getLatestNotifications(principal.getId(), limit);
        return ResponseEntity.ok(ApiResponse.success("Latest notifications retrieved", notifications));
    }

    // =========================================================================
    // GET /api/v1/notifications/unread-count
    // =========================================================================

    @GetMapping("/unread-count")
    @Operation(summary = "Get unread notification count for the bell badge")
    public ResponseEntity<ApiResponse<UnreadCountResponse>> getUnreadCount(
            @AuthenticationPrincipal UserPrincipal principal) {

        UnreadCountResponse response = notificationQueryService.getUnreadCount(principal.getId());
        return ResponseEntity.ok(ApiResponse.success("Unread count retrieved", response));
    }

    // =========================================================================
    // PUT /api/v1/notifications/{id}/read
    // =========================================================================

    @PutMapping("/{id}/read")
    @Operation(summary = "Mark a single notification as read")
    public ResponseEntity<ApiResponse<NotificationResponse>> markAsRead(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable String id) {

        NotificationResponse response = notificationCommandService.markAsRead(principal.getId(), id);
        return ResponseEntity.ok(ApiResponse.success("Notification marked as read", response));
    }

    // =========================================================================
    // PUT /api/v1/notifications/read-all
    // =========================================================================

    @PutMapping("/read-all")
    @Operation(summary = "Mark all unread notifications as read")
    public ResponseEntity<ApiResponse<Void>> markAllAsRead(
            @AuthenticationPrincipal UserPrincipal principal) {

        notificationCommandService.markAllAsRead(principal.getId());
        return ResponseEntity.ok(ApiResponse.success("All notifications marked as read"));
    }

    // =========================================================================
    // DELETE /api/v1/notifications/{id}
    // =========================================================================

    @DeleteMapping("/{id}")
    @Operation(summary = "Archive (soft-delete) a notification")
    public ResponseEntity<ApiResponse<Void>> archiveNotification(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable String id) {

        notificationCommandService.archiveNotification(principal.getId(), id);
        return ResponseEntity.ok(ApiResponse.success("Notification archived"));
    }
}
