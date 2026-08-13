package com.nexus.agent.runtime;

import com.nexus.agent.action.AgentAction;
import com.nexus.agent.action.AgentActionResultService;
import com.nexus.agent.action.AgentActionType;
import com.nexus.agent.client.NexusApiClient;
import com.nexus.agent.llm.TextGenerator;
import com.nexus.agent.profile.AgentProfile;
import com.nexus.agent.profile.AgentProfileRepository;
import com.nexus.agent.profile.AgentTokenService;
import com.nexus.agent.tool.FollowTool;
import com.nexus.agent.tool.MessagingTool;
import com.nexus.agent.tool.TimelineTool;
import com.nexus.agent.tool.TweetTool;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.ThreadLocalRandom;
import org.springframework.stereotype.Service;

@Service
public class AgentRuntime {
    private static final int MAX_AGENT_CONTENT_LENGTH = 280;
    private final AgentProfileRepository profileRepository;
    private final AgentTokenService tokenService;
    private final TweetTool tweetTool;
    private final TimelineTool timelineTool;
    private final FollowTool followTool;
    private final MessagingTool messagingTool;
    private final TextGenerator textGenerator;
    private final AgentActionResultService resultService;
    private final ConcurrentHashMap<UUID, ReentrantLock> agentLocks = new ConcurrentHashMap<>();

    public AgentRuntime(AgentProfileRepository profileRepository, AgentTokenService tokenService, TweetTool tweetTool,
                        TimelineTool timelineTool, FollowTool followTool, MessagingTool messagingTool, TextGenerator textGenerator,
                        AgentActionResultService resultService) {
        this.profileRepository = profileRepository;
        this.tokenService = tokenService;
        this.tweetTool = tweetTool;
        this.timelineTool = timelineTool;
        this.followTool = followTool;
        this.messagingTool = messagingTool;
        this.textGenerator = textGenerator;
        this.resultService = resultService;
    }

    public void execute(AgentAction action) {
        ReentrantLock lock = agentLocks.computeIfAbsent(action.agentId(), ignored -> new ReentrantLock());
        lock.lock();
        try {
            if (resultService.isCompleted(action.actionId())) {
                return;
            }
            AgentProfile profile = profileRepository.findByUserId(action.agentId())
                    .orElseThrow(() -> new IllegalArgumentException("agent profile not found"));
            String token = tokenService.tokenFor(profile);
            switch (action.actionType()) {
                case TWEET -> tweet(action, profile, token);
                case SEND_DM -> sendDirectMessage(action, profile, token);
                case READ_TIMELINE -> timelineTool.fetch(token, 20);
                case FOLLOW -> follow(action, profile, token);
            }
            resultService.recordSuccess(action);
        } catch (RuntimeException exception) {
            resultService.recordFailure(action, exception);
            throw exception;
        } finally {
            lock.unlock();
        }
    }

    private void tweet(AgentAction action, AgentProfile profile, String token) {
        List<NexusApiClient.BackendTweet> timeline = timelineTool.fetch(token, 20);
        String topic = timeline.isEmpty() ? profile.getInterests().get(ThreadLocalRandom.current().nextInt(profile.getInterests().size()))
            : boundedContext(timeline.stream().map(NexusApiClient.BackendTweet::content).toList());
        String content = generatedContent("Generate one original natural social post below 280 characters. Do not copy or reuse wording"
            + " from the recent timeline, and do not use a generic example. Personality: " + profile.getPersonality()
            + ". Agent: " + profile.getUsername() + ". Interests: " + String.join(", ", profile.getInterests())
            + ". Recent timeline: " + topic + ". Unique action id: " + action.actionId() + ".");
        tweetTool.post(token, content, action.actionId().toString());
    }

    private void sendDirectMessage(AgentAction action, AgentProfile profile, String token) {
        UUID recipientId = targetId(action, profile);
        List<NexusApiClient.BackendMessage> history = messagingTool.history(token, recipientId);
        String conversation = history.isEmpty() ? "There is no previous conversation; start a natural conversation now."
            : "Recent conversation: " + boundedContext(history.stream().map(NexusApiClient.BackendMessage::content).toList()) + ".";
        String content = generatedContent("Write the actual short direct message to send, under 280 characters."
            + " Do not explain your task, mention prompts or context, ask the user to provide context, or describe that"
            + " there is no previous conversation. Reply as the agent in a natural, friendly way. Personality: "
            + profile.getPersonality() + ". Agent: " + profile.getUsername() + ". " + conversation);
        messagingTool.send(token, recipientId, content, action.actionId().toString());
    }

    private void follow(AgentAction action, AgentProfile profile, String token) {
        followTool.follow(token, targetId(action, profile), action.actionId().toString());
    }

    private UUID targetId(AgentAction action, AgentProfile profile) {
        String target = action.metadata().get("targetUserId");
        if (target == null) {
            throw new IllegalArgumentException("target user is required for " + action.actionType());
        }
        UUID targetId = UUID.fromString(target);
        if (targetId.equals(profile.getUserId())) {
            throw new IllegalArgumentException("agents cannot target themselves");
        }
        return targetId;
    }

    private String generatedContent(String prompt) {
        String content = textGenerator.generate(prompt).trim();
        if (content.isEmpty() || content.length() > MAX_AGENT_CONTENT_LENGTH) {
            throw new IllegalArgumentException("generated content must contain between 1 and 280 characters");
        }
        return content;
    }

    private String boundedContext(List<String> content) {
        String joined = String.join("\n", content);
        return joined.length() <= 1_000 ? joined : joined.substring(0, 1_000);
    }
}