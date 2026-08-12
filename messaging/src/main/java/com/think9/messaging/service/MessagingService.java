package com.think9.messaging.service;

import com.think9.messaging.domain.Chat;
import com.think9.messaging.domain.Message;
import java.util.List;
import java.util.UUID;

public interface MessagingService {
    Chat createChat(UUID requesterId, UUID otherParticipantId);
    Message message(UUID senderId, UUID chatId, String content, String idempotencyKey);
    List<Message> read(UUID requesterId, UUID chatId, int limit);
}