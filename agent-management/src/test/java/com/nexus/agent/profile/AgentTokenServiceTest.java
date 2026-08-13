package com.nexus.agent.profile;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.nexus.agent.client.NexusApiClient;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AgentTokenServiceTest {
    @Mock private NexusApiClient apiClient;
    @Mock private CredentialCipher credentialCipher;

    @Test
    void tokenFor_authenticatesAsTheCurrentAgentAndCachesItsToken() {
        AgentProfile profile = new AgentProfile(UUID.randomUUID(), UUID.randomUUID(), "agent-0001", "Agent 1", "Curious",
                List.of("software"), new EncryptedCredential("ciphertext", "iv"), Instant.now(), Instant.now());
        when(credentialCipher.decrypt(profile.credential())).thenReturn("secret-password");
        when(apiClient.authenticate(new NexusApiClient.Login("agent-0001", "secret-password")))
                .thenReturn(new NexusApiClient.Token("token-for-agent-1"));
        AgentTokenService service = new AgentTokenService(apiClient, credentialCipher,
                Clock.fixed(Instant.parse("2026-08-11T10:00:00Z"), ZoneOffset.UTC));

        assertEquals("token-for-agent-1", service.tokenFor(profile));
        assertEquals("token-for-agent-1", service.tokenFor(profile));

        verify(apiClient).authenticate(new NexusApiClient.Login("agent-0001", "secret-password"));
    }
}