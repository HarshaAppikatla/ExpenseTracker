package com.expenseflow.notification.sse;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("SseEmitterRegistry unit tests")
class SseEmitterRegistryTest {

    private SseEmitterRegistry registry;

    @BeforeEach
    void setUp() {
        registry = new SseEmitterRegistry();
    }

    @Test
    @DisplayName("Should successfully add and retrieve emitters")
    void testAddAndGet() {
        SseEmitter emitter1 = new SseEmitter();
        SseEmitter emitter2 = new SseEmitter();

        registry.add("user-123", emitter1);
        registry.add("user-123", emitter2);

        List<SseEmitter> active = registry.get("user-123");
        assertThat(active).hasSize(2).contains(emitter1, emitter2);
    }

    @Test
    @DisplayName("Should return empty list for unknown user")
    void testGetUnknownUser() {
        List<SseEmitter> active = registry.get("unknown");
        assertThat(active).isEmpty();
    }

    @Test
    @DisplayName("Should clean up empty list when all emitters are removed")
    void testRemove() {
        SseEmitter emitter1 = new SseEmitter();
        registry.add("user-123", emitter1);

        registry.remove("user-123", emitter1);

        assertThat(registry.get("user-123")).isEmpty();
        assertThat(registry.getAll()).isEmpty();
    }

    @Test
    @DisplayName("Should evict oldest emitter when limit of 10 is exceeded")
    void testMaxEmittersEviction() {
        for (int i = 0; i < 10; i++) {
            registry.add("user-123", new SseEmitter());
        }

        List<SseEmitter> activeBefore = registry.get("user-123");
        assertThat(activeBefore).hasSize(10);

        SseEmitter oldest = activeBefore.get(0);
        SseEmitter newest = new SseEmitter();

        registry.add("user-123", newest);

        List<SseEmitter> activeAfter = registry.get("user-123");
        assertThat(activeAfter).hasSize(10);
        assertThat(activeAfter).doesNotContain(oldest);
        assertThat(activeAfter.get(9)).isEqualTo(newest);
    }
}
