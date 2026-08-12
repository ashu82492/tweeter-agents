package com.think9.agent.runtime;

import com.think9.agent.action.AgentAction;
import com.think9.agent.action.AgentActionResultService;
import com.think9.agent.action.AgentActionType;
import com.think9.agent.client.Think9ApiClient;
import com.think9.agent.llm.TextGenerator;
import com.think9.agent.profile.AgentProfile;
import com.think9.agent.profile.AgentProfileRepository;
import com.think9.agent.profile.AgentTokenService;
import com.think9.agent.tool.FollowTool;
import com.think9.agent.tool.MessagingTool;
import com.think9.agent.tool.TimelineTool;
import com.think9.agent.tool.TweetTool;
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
        List<Think9ApiClient.BackendTweet> timeline = timelineTool.fetch(token, 20);
        String topic = timeline.isEmpty() ? profile.getInterests().get(ThreadLocalRandom.current().nextInt(profile.getInterests().size()))
            : boundedContext(timeline.stream().map(Think9ApiClient.BackendTweet::content).toList());
        String content = generatedContent("Generate one natural social post below 280 characters. Personality: " + profile.getPersonality()
            + ". Interests: " + String.join(", ", profile.getInterests()) + ". Recent timeline: " + topic + ".");
        tweetTool.post(token, content, action.actionId().toString());
    }

    private void sendDirectMessage(AgentAction action, AgentProfile profile, String token) {
        UUID recipientId = targetId(action, profile);
        List<Think9ApiClient.BackendMessage> history = messagingTool.history(token, recipientId);
        String content = generatedContent("Generate one short direct message. Personality: " + profile.getPersonality()
            + ". Recent conversation: " + boundedContext(history.stream().map(Think9ApiClient.BackendMessage::content).toList()) + ".");
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