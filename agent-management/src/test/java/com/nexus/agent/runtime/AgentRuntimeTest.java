package com.nexus.agent.runtime;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.nexus.agent.action.AgentAction;
import com.nexus.agent.action.AgentActionResultService;
import com.nexus.agent.action.AgentActionType;
import com.nexus.agent.llm.TextGenerator;
import com.nexus.agent.profile.AgentProfile;
import com.nexus.agent.profile.AgentProfileRepository;
import com.nexus.agent.profile.AgentTokenService;
import com.nexus.agent.profile.EncryptedCredential;
import com.nexus.agent.tool.FollowTool;
import com.nexus.agent.tool.MessagingTool;
import com.nexus.agent.tool.TimelineTool;
import com.nexus.agent.tool.TweetTool;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AgentRuntimeTest {
    @Mock private AgentProfileRepository profileRepository;
    @Mock private AgentTokenService tokenService;
    @Mock private TweetTool tweetTool;
    @Mock private TimelineTool timelineTool;
    @Mock private FollowTool followTool;
    @Mock private MessagingTool messagingTool;
    @Mock private TextGenerator textGenerator;
    @Mock private AgentActionResultService resultService;

    @Test
    void execute_rejectsDirectMessageToSelfAndRecordsFailure() {
        AgentProfile profile = profile();
        AgentAction action = new AgentAction(UUID.randomUUID(), profile.getUserId(), AgentActionType.SEND_DM,
                Instant.now(), Map.of("targetUserId", profile.getUserId().toString()));
        when(resultService.isCompleted(action.actionId())).thenReturn(false);
        when(profileRepository.findByUserId(profile.getUserId())).thenReturn(Optional.of(profile));
        when(tokenService.tokenFor(profile)).thenReturn("agent-token");
        AgentRuntime runtime = runtime();

        assertThrows(IllegalArgumentException.class, () -> runtime.execute(action));

        verify(resultService).recordFailure(org.mockito.ArgumentMatchers.eq(action),
            org.mockito.ArgumentMatchers.any(IllegalArgumentException.class));
    }

    @Test
    void execute_rejectsFollowOfSelfAndRecordsFailure() {
        AgentProfile profile = profile();
        AgentAction action = new AgentAction(UUID.randomUUID(), profile.getUserId(), AgentActionType.FOLLOW,
                Instant.now(), Map.of("targetUserId", profile.getUserId().toString()));
        when(resultService.isCompleted(action.actionId())).thenReturn(false);
        when(profileRepository.findByUserId(profile.getUserId())).thenReturn(Optional.of(profile));
        when(tokenService.tokenFor(profile)).thenReturn("agent-token");
        AgentRuntime runtime = runtime();

        assertThrows(IllegalArgumentException.class, () -> runtime.execute(action));

        verify(resultService).recordFailure(org.mockito.ArgumentMatchers.eq(action),
            org.mockito.ArgumentMatchers.any(IllegalArgumentException.class));
    }

    @Test
    void execute_tweetPromptRequestsOriginalAgentSpecificContent() {
        AgentProfile profile = profile();
        AgentAction action = new AgentAction(UUID.randomUUID(), profile.getUserId(), AgentActionType.TWEET,
                Instant.now(), Map.of());
        when(resultService.isCompleted(action.actionId())).thenReturn(false);
        when(profileRepository.findByUserId(profile.getUserId())).thenReturn(Optional.of(profile));
        when(tokenService.tokenFor(profile)).thenReturn("agent-token");
        when(timelineTool.fetch("agent-token", 20)).thenReturn(List.of());
        when(textGenerator.generate(org.mockito.ArgumentMatchers.anyString())).thenReturn("A fresh post");

        runtime().execute(action);

        org.mockito.ArgumentCaptor<String> prompt = org.mockito.ArgumentCaptor.forClass(String.class);
        verify(textGenerator).generate(prompt.capture());
        assertTrue(prompt.getValue().contains("original"));
        assertTrue(prompt.getValue().contains(profile.getUsername()));
        assertTrue(prompt.getValue().contains(action.actionId().toString()));
    }

    @Test
    void execute_directMessagePromptRequestsNaturalMessageWhenConversationIsEmpty() {
        AgentProfile profile = profile();
        UUID recipientId = UUID.randomUUID();
        AgentAction action = new AgentAction(UUID.randomUUID(), profile.getUserId(), AgentActionType.SEND_DM,
                Instant.now(), Map.of("targetUserId", recipientId.toString()));
        when(resultService.isCompleted(action.actionId())).thenReturn(false);
        when(profileRepository.findByUserId(profile.getUserId())).thenReturn(Optional.of(profile));
        when(tokenService.tokenFor(profile)).thenReturn("agent-token");
        when(messagingTool.history("agent-token", recipientId)).thenReturn(List.of());
        when(textGenerator.generate(org.mockito.ArgumentMatchers.anyString())).thenReturn("Hey, how is your day going?");

        runtime().execute(action);

        org.mockito.ArgumentCaptor<String> prompt = org.mockito.ArgumentCaptor.forClass(String.class);
        verify(textGenerator).generate(prompt.capture());
        assertTrue(prompt.getValue().contains("actual short direct message"));
        assertTrue(prompt.getValue().contains("start a natural conversation now"));
        assertTrue(prompt.getValue().contains("Do not explain your task"));
        assertTrue(prompt.getValue().contains(profile.getUsername()));
    }

    private AgentRuntime runtime() {
        return new AgentRuntime(profileRepository, tokenService, tweetTool, timelineTool, followTool, messagingTool,
                textGenerator, resultService);
    }

    private AgentProfile profile() {
        return new AgentProfile(UUID.randomUUID(), UUID.randomUUID(), "agent-0001", "Agent 1", "Curious",
                List.of("software"), new EncryptedCredential("ciphertext", "iv"), Instant.now(), Instant.now());
    }
}