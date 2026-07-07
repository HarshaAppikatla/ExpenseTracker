package com.expenseflow.notification.sse;

import com.expenseflow.notification.domain.event.NotificationCreatedEvent;
import com.expenseflow.notification.dto.NotificationResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;

/**
 * Handles broadcasting notification DTO payloads to active SSE streams.
 *
 * <p>Listens exclusively to {@link NotificationCreatedEvent} which decoupled contexts trigger.
 * It is completely isolated from Expense/Group business rules.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SsePublisher {

    private final SseEmitterRegistry emitterRegistry;

    /**
     * Listens to the NotificationCreatedEvent and pushes the notification DTO to the active client emitters.
     * Runs asynchronously in a task executor thread to prevent blocking the command request path.
     */
    @Async
    @EventListener
    public void handleNotificationCreatedEvent(NotificationCreatedEvent event) {
        String userId = event.getUserId();
        NotificationResponse response = event.getNotification();

        List<SseEmitter> emitters = emitterRegistry.get(userId);
        if (emitters.isEmpty()) {
            log.trace("No active SSE emitters found for user {}. Notification will not be streamed.", userId);
            return;
        }

        log.debug("Streaming notification to {} active connection(s) for user {}", emitters.size(), userId);

        for (SseEmitter emitter : emitters) {
            try {
                emitter.send(SseEmitter.event()
                        .name("notification")
                        .id(event.getNotificationId())
                        .data(response));
            } catch (IOException | IllegalStateException e) {
                log.debug("Failed to push notification to emitter for user {}: {}. Removing emitter.",
                        userId, e.getMessage());
                emitterRegistry.remove(userId, emitter);
            }
        }
    }
}
