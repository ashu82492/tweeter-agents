package com.think9.messaging.repository;

import com.think9.messaging.domain.Chat;
import java.util.Optional;
import java.util.UUID;

public interface ChatRepository {
    Optional<Chat> findByParticipantPairKey(String participantPairKey);
    Optional<Chat> findById(UUID chatId);
    Chat save(Chat chat);
}