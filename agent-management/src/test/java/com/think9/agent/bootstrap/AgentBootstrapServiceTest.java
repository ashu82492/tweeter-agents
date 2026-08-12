package com.think9.agent.bootstrap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.think9.agent.client.Think9ApiClient;
import com.think9.agent.config.AgentManagementProperties;
import com.think9.agent.profile.AgentProfile;
import com.think9.agent.profile.AgentProfileRepository;
import com.think9.agent.profile.AgentTokenService;
import com.think9.agent.profile.CredentialCipher;
import com.think9.agent.profile.EncryptedCredential;
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
    @Mock private Think9ApiClient apiClient;
    @Mock private CredentialCipher credentialCipher;
    @Mock private AgentTokenService tokenService;

    @Test
    void bootstrapUsesTheConfiguredPasswordForEveryAgentRegistration() {
        AgentManagementProperties properties = new AgentManagementProperties();
        properties.setAgentPassword("shared-agent-password");
        when(profileRepository.findByUsername(any())).thenReturn(Optional.empty());
        when(apiClient.register(any())).thenAnswer(invocation -> {
            Think9ApiClient.Registration registration = invocation.getArgument(0);
            return new Think9ApiClient.BackendUser(UUID.randomUUID(), registration.username(), registration.displayName(), "AGENT");
        });
        when(credentialCipher.encrypt("shared-agent-password"))
                .thenReturn(new EncryptedCredential("ciphertext", "iv"));
        when(profileRepository.save(any(AgentProfile.class))).thenAnswer(invocation -> invocation.getArgument(0));

        AgentBootstrapService service = new AgentBootstrapService(profileRepository, apiClient, credentialCipher,
                tokenService, properties, Clock.fixed(Instant.EPOCH, ZoneOffset.UTC), new ZeroRandomGenerator());

        service.bootstrap(2);

        ArgumentCaptor<Think9ApiClient.Registration> registrations = ArgumentCaptor.forClass(Think9ApiClient.Registration.class);
        verify(apiClient, org.mockito.Mockito.times(2)).register(registrations.capture());
        assertEquals(List.of("shared-agent-password", "shared-agent-password"),
                registrations.getAllValues().stream().map(Think9ApiClient.Registration::password).toList());
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
