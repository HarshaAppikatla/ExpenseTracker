package com.expenseflow.notification.sse;

import com.expenseflow.notification.domain.event.NotificationCreatedEvent;
import com.expenseflow.notification.domain.valueobject.NotificationCategory;
import com.expenseflow.notification.domain.valueobject.NotificationPriority;
import com.expenseflow.notification.domain.valueobject.NotificationStatus;
import com.expenseflow.notification.domain.valueobject.NotificationType;
import com.expenseflow.notification.dto.NotificationResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("SsePublisher unit tests")
class SsePublisherTest {

    @Mock
    private SseEmitterRegistry emitterRegistry;

    @InjectMocks
    private SsePublisher ssePublisher;

    @Test
    @DisplayName("Should broadcast notification to all active emitters of the user")
    void testHandleNotificationCreatedEvent_Success() throws IOException {
        SseEmitter mockEmitter = mock(SseEmitter.class);
        when(emitterRegistry.get("user-123")).thenReturn(List.of(mockEmitter));

        NotificationResponse response = new NotificationResponse(
                "notif-1", "Title", "Message", NotificationStatus.UNREAD,
                NotificationPriority.NORMAL, NotificationCategory.EXPENSE,
                NotificationType.EXPENSE_POSTED, null, null, null, LocalDateTime.now()
        );

        NotificationCreatedEvent event = new NotificationCreatedEvent(
                this, "notif-1", "user-123", response
        );

        ssePublisher.handleNotificationCreatedEvent(event);

        verify(mockEmitter, times(1)).send(any(SseEmitter.SseEventBuilder.class));
    }

    @Test
    @DisplayName("Should handle IOException and remove emitter from registry")
    void testHandleNotificationCreatedEvent_Error() throws IOException {
        SseEmitter mockEmitter = mock(SseEmitter.class);
        doThrow(new IOException("Connection reset")).when(mockEmitter).send(any(SseEmitter.SseEventBuilder.class));
        when(emitterRegistry.get("user-123")).thenReturn(List.of(mockEmitter));

        NotificationResponse response = new NotificationResponse(
                "notif-1", "Title", "Message", NotificationStatus.UNREAD,
                NotificationPriority.NORMAL, NotificationCategory.EXPENSE,
                NotificationType.EXPENSE_POSTED, null, null, null, LocalDateTime.now()
        );

        NotificationCreatedEvent event = new NotificationCreatedEvent(
                this, "notif-1", "user-123", response
        );

        ssePublisher.handleNotificationCreatedEvent(event);

        verify(emitterRegistry, times(1)).remove("user-123", mockEmitter);
    }
}
