package com.nexus.agent.kafka;

import com.nexus.agent.action.AgentAction;
import com.nexus.agent.metrics.RuntimeErrorReporter;
import com.nexus.agent.runtime.AgentRuntime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

@Component
public class AgentActionWorker {
    private static final Logger LOGGER = LoggerFactory.getLogger(AgentActionWorker.class);
    private final AgentRuntime agentRuntime;
    private final RuntimeErrorReporter errorReporter;

    public AgentActionWorker(AgentRuntime agentRuntime, RuntimeErrorReporter errorReporter) {
        this.agentRuntime = agentRuntime;
        this.errorReporter = errorReporter;
    }

    @KafkaListener(topics = "${nexus.agent.kafka.topic:agent-actions}", groupId = "agent-workers",
            concurrency = "${nexus.agent.kafka.worker-count:1}")
    public void consume(AgentAction action, Acknowledgment acknowledgment) {
        try {
            agentRuntime.execute(action);
            LOGGER.info("Agent action completed agentId={} actionId={} type={}", action.agentId(), action.actionId(), action.actionType());
        } catch (RuntimeException exception) {
            LOGGER.warn("Agent action failed agentId={} actionId={} type={}",
                    action.agentId(), action.actionId(), action.actionType(), exception);
            try {
                errorReporter.report();
            } catch (RuntimeException reporterException) {
                LOGGER.warn("Unable to report agent runtime failure actionId={}", action.actionId());
            }
        } finally {
            acknowledgment.acknowledge();
        }
    }
}