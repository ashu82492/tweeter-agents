package com.nexus.messaging.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.nexus.core.events.EventPublisher;
import com.nexus.messaging.domain.Chat;
import com.nexus.messaging.domain.Message;
import com.nexus.messaging.repository.ChatRepository;
import com.nexus.messaging.repository.MessageRepository;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class MessagingServiceImplTest {
    @Mock private ChatRepository chatRepository;
    @Mock private MessageRepository messageRepository;
    @Mock private EventPublisher eventPublisher;

    @Test
    void createChat_returnsExistingChat_whenPairAlreadyExists() {
        UUID requester = UUID.randomUUID();
        UUID other = UUID.randomUUID();
        Chat existing = new Chat(UUID.randomUUID(), "pair", Set.of(requester, other), Instant.EPOCH);
        when(chatRepository.findByParticipantPairKey(org.mockito.ArgumentMatchers.anyString())).thenReturn(Optional.of(existing));
        MessagingService service = new MessagingServiceImpl(chatRepository, messageRepository, eventPublisher, Clock.systemUTC());

        assertEquals(existing, service.createChat(requester, other));
        verify(chatRepository, never()).save(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void read_rejectsNonParticipant() {
        UUID participant = UUID.randomUUID();
        UUID outsider = UUID.randomUUID();
        Chat chat = new Chat(UUID.randomUUID(), "pair", Set.of(participant, UUID.randomUUID()), Instant.EPOCH);
        when(chatRepository.findById(chat.id())).thenReturn(Optional.of(chat));
        MessagingService service = new MessagingServiceImpl(chatRepository, messageRepository, eventPublisher, Clock.systemUTC());

        assertThrows(SecurityException.class, () -> service.read(outsider, chat.id(), 20));
    }

    @Test
    void message_publishesEventOnlyWhenPersistingANewMessage() {
        UUID sender = UUID.randomUUID();
        UUID chatId = UUID.randomUUID();
        Chat chat = new Chat(chatId, "pair", Set.of(sender, UUID.randomUUID()), Instant.EPOCH);
        Message existing = new Message(UUID.randomUUID(), chatId, sender, "existing", "duplicate", Instant.EPOCH);
        when(chatRepository.findById(chatId)).thenReturn(Optional.of(chat));
        when(messageRepository.findByChatIdAndSenderIdAndIdempotencyKey(chatId, sender, "duplicate")).thenReturn(Optional.of(existing));
        MessagingService service = new MessagingServiceImpl(chatRepository, messageRepository, eventPublisher, Clock.systemUTC());

        assertEquals(existing, service.message(sender, chatId, "ignored", "duplicate"));

        verify(eventPublisher, never()).publish(org.mockito.ArgumentMatchers.any());
    }
}