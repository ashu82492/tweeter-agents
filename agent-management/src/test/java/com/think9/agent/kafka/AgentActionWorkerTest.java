package com.think9.agent.kafka;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

import com.think9.agent.action.AgentAction;
import com.think9.agent.action.AgentActionType;
import com.think9.agent.runtime.AgentRuntime;
import com.think9.agent.metrics.RuntimeErrorReporter;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.support.Acknowledgment;

@ExtendWith(MockitoExtension.class)
class AgentActionWorkerTest {
    @Mock private AgentRuntime runtime;
    @Mock private RuntimeErrorReporter errorReporter;
    @Mock private Acknowledgment acknowledgment;

    @Test
    void consume_acknowledgesFailedActionSoOtherWorkersContinue() {
        AgentAction action = new AgentAction(UUID.randomUUID(), UUID.randomUUID(), AgentActionType.TWEET, Instant.now(), Map.of());
        doThrow(new IllegalStateException("LM Studio unavailable")).when(runtime).execute(action);

        new AgentActionWorker(runtime, errorReporter).consume(action, acknowledgment);

        verify(errorReporter).report();
        verify(acknowledgment).acknowledge();
    }
}