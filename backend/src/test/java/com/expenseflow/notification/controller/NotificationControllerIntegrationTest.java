package com.expenseflow.notification.controller;

import com.expenseflow.config.SecurityProperties;
import com.expenseflow.entity.UserEntity;
import com.expenseflow.notification.domain.valueobject.*;
import com.expenseflow.notification.dto.NotificationResponse;
import com.expenseflow.notification.dto.UnreadCountResponse;
import com.expenseflow.notification.service.NotificationCommandService;
import com.expenseflow.notification.service.NotificationQueryService;
import com.expenseflow.security.JwtAuthenticationFilter;
import com.expenseflow.security.JwtService;
import com.expenseflow.security.UserDetailsServiceImpl;
import com.expenseflow.security.UserPrincipal;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(NotificationController.class)
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("NotificationController mock MVC slice tests")
class NotificationControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private NotificationCommandService notificationCommandService;

    @MockBean
    private NotificationQueryService notificationQueryService;

    // Spring security mocks
    @MockBean private JwtService jwtService;
    @MockBean private UserDetailsServiceImpl userDetailsService;
    @MockBean private SecurityProperties securityProperties;
    @MockBean private JwtAuthenticationFilter jwtAuthenticationFilter;

    private UserPrincipal userPrincipal;
    private NotificationResponse testResponse;

    @BeforeEach
    void setUp() {
        UserEntity userEntity = new UserEntity();
        userEntity.setId("user-123");
        userEntity.setEmail("user@example.com");
        userEntity.setRoles(Collections.emptySet());
        userPrincipal = new UserPrincipal(userEntity);

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(userPrincipal, null, userPrincipal.getAuthorities())
        );

        testResponse = new NotificationResponse(
                "notif-123", "Title 1", "Msg 1",
                NotificationStatus.UNREAD, NotificationPriority.NORMAL,
                NotificationCategory.EXPENSE, NotificationType.EXPENSE_POSTED,
                null, null, null, LocalDateTime.now()
        );
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("GET /api/v1/notifications - should return paginated notifications")
    void testGetNotifications_Success() throws Exception {
        PageImpl<NotificationResponse> page = new PageImpl<>(List.of(testResponse));
        when(notificationQueryService.getNotifications(eq("user-123"), any())).thenReturn(page);

        mockMvc.perform(get("/api/v1/notifications"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content[0].title").value("Title 1"));
    }

    @Test
    @DisplayName("GET /api/v1/notifications/unread-count - should return correct count")
    void testGetUnreadCount_Success() throws Exception {
        UnreadCountResponse response = new UnreadCountResponse(3L);
        when(notificationQueryService.getUnreadCount("user-123")).thenReturn(response);

        mockMvc.perform(get("/api/v1/notifications/unread-count"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.unreadCount").value(3));
    }

    @Test
    @DisplayName("GET /api/v1/notifications/latest - should return latest notifications list")
    void testGetLatestNotifications_Success() throws Exception {
        when(notificationQueryService.getLatestNotifications("user-123", 5))
                .thenReturn(List.of(testResponse));

        mockMvc.perform(get("/api/v1/notifications/latest?limit=5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.length()").value(1));
    }

    @Test
    @DisplayName("PUT /api/v1/notifications/{id}/read - should mark notification as read")
    void testMarkAsRead_Success() throws Exception {
        NotificationResponse readResponse = new NotificationResponse(
                "notif-123", "Title 1", "Msg 1",
                NotificationStatus.READ, NotificationPriority.NORMAL,
                NotificationCategory.EXPENSE, NotificationType.EXPENSE_POSTED,
                null, null, null, LocalDateTime.now()
        );
        when(notificationCommandService.markAsRead("user-123", "notif-123")).thenReturn(readResponse);

        mockMvc.perform(put("/api/v1/notifications/notif-123/read")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("READ"));
    }

    @Test
    @DisplayName("PUT /api/v1/notifications/read-all - should mark all notifications as read")
    void testMarkAllAsRead_Success() throws Exception {
        when(notificationCommandService.markAllAsRead("user-123")).thenReturn(2);

        mockMvc.perform(put("/api/v1/notifications/read-all")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        verify(notificationCommandService).markAllAsRead("user-123");
    }

    @Test
    @DisplayName("DELETE /api/v1/notifications/{id} - should archive notification")
    void testArchiveNotification_Success() throws Exception {
        mockMvc.perform(delete("/api/v1/notifications/notif-123")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        verify(notificationCommandService).archiveNotification("user-123", "notif-123");
    }
}
