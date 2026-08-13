package com.nexus.messaging.service;

import com.nexus.core.events.EventPublisher;
import com.nexus.core.events.MessageCreated;
import com.nexus.messaging.domain.Chat;
import com.nexus.messaging.domain.Message;
import com.nexus.messaging.repository.ChatRepository;
import com.nexus.messaging.repository.MessageRepository;
import java.time.Clock;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MessagingServiceImpl implements MessagingService {
    private final ChatRepository chatRepository;
    private final MessageRepository messageRepository;
    private final EventPublisher eventPublisher;
    private final Clock clock;

    public MessagingServiceImpl(ChatRepository chatRepository, MessageRepository messageRepository, EventPublisher eventPublisher, Clock clock) {
        this.chatRepository = chatRepository;
        this.messageRepository = messageRepository;
        this.eventPublisher = eventPublisher;
        this.clock = clock;
    }

    @Override
    @Transactional
    public Chat createChat(UUID requesterId, UUID otherParticipantId) {
        if (requesterId.equals(otherParticipantId)) {
            throw new IllegalArgumentException("a direct chat requires two distinct participants");
        }
        String pairKey = pairKey(requesterId, otherParticipantId);
        return chatRepository.findByParticipantPairKey(pairKey).orElseGet(() ->
                chatRepository.save(new Chat(UUID.randomUUID(), pairKey, Set.of(requesterId, otherParticipantId), clock.instant())));
    }

    @Override
    @Transactional
    public Message message(UUID senderId, UUID chatId, String content, String idempotencyKey) {
        Chat chat = requireParticipant(senderId, chatId);
        if (content == null || content.isBlank() || content.length() > 4_000) {
            throw new IllegalArgumentException("message content must contain between 1 and 4000 characters");
        }
        return messageRepository.findByChatIdAndSenderIdAndIdempotencyKey(chat.id(), senderId, idempotencyKey)
            .orElseGet(() -> createMessage(senderId, chat.id(), content, idempotencyKey));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Message> read(UUID requesterId, UUID chatId, int limit) {
        requireParticipant(requesterId, chatId);
        return messageRepository.findByChatId(chatId, Math.min(Math.max(limit, 1), 100));
    }

    private Chat requireParticipant(UUID requesterId, UUID chatId) {
        Chat chat = chatRepository.findById(chatId).orElseThrow(() -> new IllegalArgumentException("chat not found"));
        if (!chat.participantIds().contains(requesterId)) {
            throw new SecurityException("only chat participants may access messages");
        }
        return chat;
    }

    private Message createMessage(UUID senderId, UUID chatId, String content, String idempotencyKey) {
        Message savedMessage = messageRepository.save(new Message(UUID.randomUUID(), chatId, senderId,
                content.trim(), idempotencyKey, clock.instant()));
        eventPublisher.publish(new MessageCreated(UUID.randomUUID(), savedMessage.id(), senderId,
                savedMessage.createdAt(), idempotencyKey));
        return savedMessage;
    }

    private String pairKey(UUID first, UUID second) {
        return first.compareTo(second) < 0 ? first + ":" + second : second + ":" + first;
    }
}