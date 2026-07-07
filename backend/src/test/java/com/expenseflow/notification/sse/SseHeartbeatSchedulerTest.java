package com.expenseflow.notification.sse;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("SseHeartbeatScheduler unit tests")
class SseHeartbeatSchedulerTest {

    @Mock
    private SseEmitterRegistry emitterRegistry;

    @InjectMocks
    private SseHeartbeatScheduler scheduler;

    @Test
    @DisplayName("Should send heartbeats to all registered user emitters")
    void testSendHeartbeats_Success() throws IOException {
        SseEmitter mockEmitter = mock(SseEmitter.class);
        when(emitterRegistry.getAll()).thenReturn(Map.of("user-123", List.of(mockEmitter)));

        scheduler.sendHeartbeats();

        verify(mockEmitter, times(1)).send(any(SseEmitter.SseEventBuilder.class));
    }

    @Test
    @DisplayName("Should handle IOException on heartbeat and remove emitter")
    void testSendHeartbeats_Error() throws IOException {
        SseEmitter mockEmitter = mock(SseEmitter.class);
        doThrow(new IOException("Broken pipe")).when(mockEmitter).send(any(SseEmitter.SseEventBuilder.class));
        when(emitterRegistry.getAll()).thenReturn(Map.of("user-123", List.of(mockEmitter)));

        scheduler.sendHeartbeats();

        verify(emitterRegistry, times(1)).remove("user-123", mockEmitter);
    }
}
