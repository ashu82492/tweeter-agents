package com.nexus.messaging.service;

import com.nexus.messaging.domain.Chat;
import com.nexus.messaging.domain.Message;
import java.util.List;
import java.util.UUID;

public interface MessagingService {
    Chat createChat(UUID requesterId, UUID otherParticipantId);
    Message message(UUID senderId, UUID chatId, String content, String idempotencyKey);
    List<Message> read(UUID requesterId, UUID chatId, int limit);
}