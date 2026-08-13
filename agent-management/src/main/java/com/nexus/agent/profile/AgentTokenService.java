package com.nexus.agent.profile;

import com.nexus.agent.client.NexusApiClient;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AgentTokenService {
    private static final Duration TOKEN_CACHE_TTL = Duration.ofMinutes(55);

    private final NexusApiClient apiClient;
    private final CredentialCipher credentialCipher;
    private final Clock clock;
    private final ConcurrentHashMap<UUID, CachedToken> tokens = new ConcurrentHashMap<>();

    @Autowired
    public AgentTokenService(NexusApiClient apiClient, CredentialCipher credentialCipher) {
        this(apiClient, credentialCipher, Clock.systemUTC());
    }

    AgentTokenService(NexusApiClient apiClient, CredentialCipher credentialCipher, Clock clock) {
        this.apiClient = apiClient;
        this.credentialCipher = credentialCipher;
        this.clock = clock;
    }

    public String tokenFor(AgentProfile profile) {
        CachedToken cachedToken = tokens.get(profile.getUserId());
        if (cachedToken != null && cachedToken.expiresAt().isAfter(clock.instant())) {
            return cachedToken.value();
        }
        String token = apiClient.authenticate(new NexusApiClient.Login(profile.getUsername(),
                credentialCipher.decrypt(profile.credential()))).accessToken();
        tokens.put(profile.getUserId(), new CachedToken(token, clock.instant().plus(TOKEN_CACHE_TTL)));
        return token;
    }

    public void invalidate(UUID agentId) {
        tokens.remove(agentId);
    }

    private record CachedToken(String value, Instant expiresAt) { }
}