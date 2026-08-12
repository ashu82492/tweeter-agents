package com.think9.app.metrics;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import com.think9.identity.domain.UserType;
import com.think9.identity.repository.UserRepository;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthEndpoint;

@ExtendWith(MockitoExtension.class)
class MetricServiceImplTest {
    private final Instant now = Instant.parse("2026-08-10T12:00:00Z");
    @Mock private UserRepository userRepository;
    @Mock private HealthEndpoint healthEndpoint;
    private MetricService metricService;

    @BeforeEach
    void setUp() {
        when(userRepository.countByType(UserType.SYSTEM_AGENT)).thenReturn(100L);
        when(userRepository.countByTypeAndLastActiveAtAfter(UserType.SYSTEM_AGENT, now.minusSeconds(600))).thenReturn(97L);
        when(healthEndpoint.health()).thenReturn(Health.up().build());
        metricService = new MetricServiceImpl(userRepository, healthEndpoint, Clock.fixed(now, ZoneOffset.UTC));
    }

    @Test
    void snapshot_returnsTrailingHourRatesAndCurrentServiceState() {
        for (int event = 0; event < 30; event++) {
            metricService.recordTweet(now);
        }
        metricService.recordDirectMessage(now.minusSeconds(61 * 60));
        metricService.recordError();

        MetricsSnapshot snapshot = metricService.snapshot();

        assertEquals(100, snapshot.agents());
        assertEquals(97, snapshot.activeAgents());
        assertEquals(1, snapshot.tweetsPerMinute());
        assertEquals(0, snapshot.dmsPerMinute());
        assertEquals(1, snapshot.errors());
        assertEquals("UP", snapshot.health());
    }
}