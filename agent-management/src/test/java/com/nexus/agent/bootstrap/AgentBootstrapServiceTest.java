package com.nexus.agent.bootstrap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.nexus.agent.client.NexusApiClient;
import com.nexus.agent.config.AgentManagementProperties;
import com.nexus.agent.profile.AgentProfile;
import com.nexus.agent.profile.AgentProfileRepository;
import com.nexus.agent.profile.AgentTokenService;
import com.nexus.agent.profile.CredentialCipher;
import com.nexus.agent.profile.EncryptedCredential;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.random.RandomGenerator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AgentBootstrapServiceTest {
    @Mock private AgentProfileRepository profileRepository;
    @Mock private NexusApiClient apiClient;
    @Mock private CredentialCipher credentialCipher;
    @Mock private AgentTokenService tokenService;

    @Test
    void bootstrapUsesTheConfiguredPasswordForEveryAgentRegistration() {
        AgentManagementProperties properties = new AgentManagementProperties();
        properties.setAgentPassword("shared-agent-password");
        when(profileRepository.findByUsername(any())).thenReturn(Optional.empty());
        when(apiClient.register(any())).thenAnswer(invocation -> {
            NexusApiClient.Registration registration = invocation.getArgument(0);
            return new NexusApiClient.BackendUser(UUID.randomUUID(), registration.username(), registration.displayName(), "AGENT");
        });
        when(credentialCipher.encrypt("shared-agent-password"))
                .thenReturn(new EncryptedCredential("ciphertext", "iv"));
        when(profileRepository.save(any(AgentProfile.class))).thenAnswer(invocation -> invocation.getArgument(0));

        AgentBootstrapService service = new AgentBootstrapService(profileRepository, apiClient, credentialCipher,
                tokenService, properties, Clock.fixed(Instant.EPOCH, ZoneOffset.UTC), new ZeroRandomGenerator());

        service.bootstrap(2);

        ArgumentCaptor<NexusApiClient.Registration> registrations = ArgumentCaptor.forClass(NexusApiClient.Registration.class);
        verify(apiClient, org.mockito.Mockito.times(2)).register(registrations.capture());
        assertEquals(List.of("shared-agent-password", "shared-agent-password"),
                registrations.getAllValues().stream().map(NexusApiClient.Registration::password).toList());
        verify(credentialCipher, org.mockito.Mockito.times(2)).encrypt("shared-agent-password");
    }

    private static final class ZeroRandomGenerator implements RandomGenerator {
        @Override
        public int nextInt(int bound) {
            return 0;
        }

        @Override
        public long nextLong() {
            return 0;
        }

        @Override
        public long nextLong(long origin, long bound) {
            return origin;
        }
    }
}
