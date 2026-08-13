package com.nexus.agent.bootstrap;

import com.nexus.agent.client.NexusApiClient;
import com.nexus.agent.config.AgentManagementProperties;
import com.nexus.agent.profile.AgentProfile;
import com.nexus.agent.profile.AgentProfileRepository;
import com.nexus.agent.profile.AgentTokenService;
import com.nexus.agent.profile.CredentialCipher;
import java.security.SecureRandom;
import java.time.Clock;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.random.RandomGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AgentBootstrapService {
    private final AgentProfileRepository profileRepository;
    private final NexusApiClient apiClient;
    private final CredentialCipher credentialCipher;
    private final AgentTokenService tokenService;
    private final AgentManagementProperties properties;
    private final Clock clock;
    private final RandomGenerator randomGenerator;

    @Autowired
    public AgentBootstrapService(AgentProfileRepository profileRepository, NexusApiClient apiClient,
                                 CredentialCipher credentialCipher, AgentTokenService tokenService,
                                 AgentManagementProperties properties) {
        this(profileRepository, apiClient, credentialCipher, tokenService, properties, Clock.systemUTC(), new SecureRandom());
    }

    AgentBootstrapService(AgentProfileRepository profileRepository, NexusApiClient apiClient,
                          CredentialCipher credentialCipher, AgentTokenService tokenService,
                          AgentManagementProperties properties, Clock clock, RandomGenerator randomGenerator) {
        this.profileRepository = profileRepository;
        this.apiClient = apiClient;
        this.credentialCipher = credentialCipher;
        this.tokenService = tokenService;
        this.properties = properties;
        this.clock = clock;
        this.randomGenerator = randomGenerator;
    }

    @Transactional
    public List<AgentProfile> bootstrap(int count) {
        if (count < 1) {
            throw new IllegalArgumentException("agent count must be positive");
        }
        List<AgentProfile> profiles = new ArrayList<>();
        for (int ordinal = 1; ordinal <= count; ordinal++) {
            profiles.add(profileFor(ordinal));
        }
        createInitialFollows(profiles);
        return List.copyOf(profiles);
    }

    private AgentProfile profileFor(int ordinal) {
        String username = "agent-%04d".formatted(ordinal);
        return profileRepository.findByUsername(username).orElseGet(() -> createProfile(ordinal, username));
    }

    private AgentProfile createProfile(int ordinal, String username) {
        AgentManagementProperties.Personality personality = properties.getPersonalities()
                .get((ordinal - 1) % properties.getPersonalities().size());
        String password = properties.getAgentPassword();
        NexusApiClient.BackendUser user = apiClient.register(new NexusApiClient.Registration(username, password,
                "Agent " + ordinal));
        AgentProfile profile = new AgentProfile(UUID.randomUUID(), user.id(), username, user.displayName(),
            personality.name(), personality.interests(), credentialCipher.encrypt(password), clock.instant(), clock.instant());
        return profileRepository.save(profile);
    }

    private void createInitialFollows(List<AgentProfile> profiles) {
        for (AgentProfile profile : profiles) {
            String token = tokenService.tokenFor(profile);
            for (AgentProfile target : targetsFor(profile, profiles)) {
                apiClient.follow(token, target.getUserId(), "bootstrap-follow-" + profile.getUserId() + "-" + target.getUserId());
            }
        }
    }

    List<AgentProfile> targetsFor(AgentProfile profile, List<AgentProfile> profiles) {
        List<AgentProfile> candidates = new ArrayList<>(profiles);
        candidates.removeIf(candidate -> candidate.getUserId().equals(profile.getUserId()));
        int targetCount = Math.min(randomGenerator.nextInt(6), candidates.size());
        List<AgentProfile> targets = new ArrayList<>(targetCount);
        for (int index = 0; index < targetCount; index++) {
            targets.add(candidates.remove(randomGenerator.nextInt(candidates.size())));
        }
        return targets;
    }

}