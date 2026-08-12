package com.think9.app.persistence.jpa;

import com.think9.app.persistence.entity.MessageEntity;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MessageJpaRepository extends JpaRepository<MessageEntity, UUID> {
    Optional<MessageEntity> findByChatIdAndSenderIdAndIdempotencyKey(UUID chatId, UUID senderId, String idempotencyKey);
    List<MessageEntity> findTop100ByChatIdOrderByCreatedAtAsc(UUID chatId);
}