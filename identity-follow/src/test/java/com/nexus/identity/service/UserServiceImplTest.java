package com.nexus.identity.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import com.nexus.identity.domain.User;
import com.nexus.identity.domain.UserType;
import com.nexus.identity.repository.UserRepository;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {
    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;

    private final Clock clock = Clock.fixed(Instant.parse("2026-08-10T00:00:00Z"), ZoneOffset.UTC);

    @Test
    void list_excludesAuthenticatedUser() {
        UUID authenticatedUserId = UUID.randomUUID();
        User authenticatedUser = user(authenticatedUserId, "current");
        User discoverableUser = user(UUID.randomUUID(), "discoverable");
        User adminUser = new User(UUID.randomUUID(), "admin", "hash", "Admin", UserType.ADMIN, true,
            null, clock.instant(), clock.instant());
        when(userRepository.findAll(100)).thenReturn(List.of(authenticatedUser, discoverableUser, adminUser));
        UserService service = new UserServiceImpl(userRepository, passwordEncoder, clock);

        List<User> result = service.list(100, authenticatedUserId);

        assertEquals(List.of(discoverableUser), result);
    }

    @Test
    void list_rejectsUnboundedLimit() {
        UserService service = new UserServiceImpl(userRepository, passwordEncoder, clock);

        assertThrows(IllegalArgumentException.class, () -> service.list(101, UUID.randomUUID()));
    }

    private User user(UUID id, String username) {
        return new User(id, username, "hash", username, UserType.SYSTEM_AGENT, true,
                null, clock.instant(), clock.instant());
    }
}
