package com.nexus.messaging.repository;

import com.nexus.messaging.domain.Message;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MessageRepository {
    Optional<Message> findByChatIdAndSenderIdAndIdempotencyKey(UUID chatId, UUID senderId, String idempotencyKey);
    Message save(Message message);
    List<Message> findByChatId(UUID chatId, int limit);
}