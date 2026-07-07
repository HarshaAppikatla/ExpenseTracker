package com.expenseflow.notification.sse;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Thread-safe registry managing active {@link SseEmitter} streams.
 *
 * <p>Supports multiple active connections per user (e.g., multiple open browser tabs).
 */
@Slf4j
@Component
public class SseEmitterRegistry {

    private final Map<String, List<SseEmitter>> registry = new ConcurrentHashMap<>();
    private static final int MAX_EMITTERS_PER_USER = 10;

    /**
     * Registers a new emitter for the given user.
     * Enforces a maximum connection limit per user (default: 10) via FIFO eviction.
     */
    public void add(String userId, SseEmitter emitter) {
        List<SseEmitter> emitters = registry.computeIfAbsent(userId, k -> new CopyOnWriteArrayList<>());
        if (emitters.size() >= MAX_EMITTERS_PER_USER) {
            log.warn("User {} reached max emitter limit of {}. Evicting oldest connection.", userId, MAX_EMITTERS_PER_USER);
            SseEmitter oldest = emitters.remove(0);
            try {
                oldest.complete();
            } catch (Exception e) {
                log.trace("Failed to complete evicted emitter cleanly: {}", e.getMessage());
            }
        }
        emitters.add(emitter);
        log.debug("Registered new SSE emitter for user {}. Total active connections: {}",
                userId, emitters.size());
    }

    /**
     * Removes a registered emitter for the given user.
     */
    public void remove(String userId, SseEmitter emitter) {
        List<SseEmitter> emitters = registry.get(userId);
        if (emitters != null) {
            emitters.remove(emitter);
            if (emitters.isEmpty()) {
                registry.remove(userId);
            }
            log.debug("Removed SSE emitter for user {}. Connection closed.", userId);
        }
    }

    /**
     * Returns all active emitters for a given user.
     */
    public List<SseEmitter> get(String userId) {
        List<SseEmitter> emitters = registry.get(userId);
        return emitters != null ? List.copyOf(emitters) : List.of();
    }

    /**
     * Returns a map of all registered users and their active emitters.
     */
    public Map<String, List<SseEmitter>> getAll() {
        return Map.copyOf(registry);
    }
}
