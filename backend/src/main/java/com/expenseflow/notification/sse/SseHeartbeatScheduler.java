package com.expenseflow.notification.sse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Periodically pushes heartbeat events to active SSE connections to keep them alive.
 *
 * <p>Pushes a dummy heartbeat signal every 30 seconds.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SseHeartbeatScheduler {

    private final SseEmitterRegistry emitterRegistry;

    /**
     * Sends a heartbeat event to all registered emitters every 30 seconds.
     *
     * <p>This prevents firewalls, proxies, and load balancers from cutting off idle HTTP connections.
     */
    @Scheduled(fixedRate = 30000)
    public void sendHeartbeats() {
        Map<String, List<SseEmitter>> activeConnections = emitterRegistry.getAll();
        if (activeConnections.isEmpty()) {
            return;
        }

        log.trace("Broadcasting heartbeats to {} active user session registries...", activeConnections.size());

        activeConnections.forEach((userId, emitters) -> {
            for (SseEmitter emitter : emitters) {
                try {
                    emitter.send(SseEmitter.event()
                            .name("heartbeat")
                            .data("{}"));
                } catch (IOException | IllegalStateException e) {
                    log.debug("Heartbeat failed for user {}: {}. Removing emitter.", userId, e.getMessage());
                    emitterRegistry.remove(userId, emitter);
                }
            }
        });
    }
}
