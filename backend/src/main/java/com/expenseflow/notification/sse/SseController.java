package com.expenseflow.notification.sse;

import com.expenseflow.security.UserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;

/**
 * Controller exposing the SSE stream endpoint for real-time notifications.
 *
 * <p>Authentication is handled transparently via Spring Security context.
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
@Tag(name = "Notifications Real-Time Stream", description = "Endpoints for establishing Server-Sent Events (SSE) connections")
public class SseController {

    private final SseEmitterRegistry emitterRegistry;

    // Default timeout of 30 minutes (1,800,000 milliseconds)
    private static final Long EMITTER_TIMEOUT = 1800000L;

    /**
     * Establishes a Server-Sent Events stream for the authenticated user.
     *
     * <p>Mime-type: {@code text/event-stream}
     */
    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @Operation(summary = "Subscribe to live notification stream")
    public SseEmitter subscribe(@AuthenticationPrincipal UserPrincipal principal) {
        String userId = principal.getId();
        SseEmitter emitter = new SseEmitter(EMITTER_TIMEOUT);

        // Register lifecycle callbacks for proper cleanup
        emitter.onCompletion(() -> {
            log.debug("SSE completion callback triggered for user {}", userId);
            emitterRegistry.remove(userId, emitter);
        });

        emitter.onTimeout(() -> {
            log.debug("SSE timeout callback triggered for user {}", userId);
            emitterRegistry.remove(userId, emitter);
            emitter.complete();
        });

        emitter.onError(ex -> {
            log.warn("SSE error callback triggered for user {}: {}", userId, ex.getMessage());
            emitterRegistry.remove(userId, emitter);
            emitter.complete();
        });

        // Add to active registry
        emitterRegistry.add(userId, emitter);

        // Send initial hand-shake connect event to establish client connection
        try {
            emitter.send(SseEmitter.event()
                    .name("connect")
                    .data("Connected successfully"));
        } catch (IOException e) {
            log.warn("Failed to send initial SSE connect event to user {}: {}", userId, e.getMessage());
            emitter.complete();
            emitterRegistry.remove(userId, emitter);
        }

        return emitter;
    }
}
