package com.think9.app.persistence.adapter;

import com.think9.app.persistence.entity.MessageEntity;
import com.think9.app.persistence.jpa.MessageJpaRepository;
import com.think9.messaging.domain.Message;
import com.think9.messaging.repository.MessageRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Repository;

@Repository
public class MessageRepositoryAdapter implements MessageRepository {
    private final MessageJpaRepository repository;
    public MessageRepositoryAdapter(MessageJpaRepository repository) { this.repository = repository; }
    @Override public Optional<Message> findByChatIdAndSenderIdAndIdempotencyKey(UUID chatId, UUID senderId, String key) { return repository.findByChatIdAndSenderIdAndIdempotencyKey(chatId, senderId, key).map(MessageEntity::toDomain); }
    @Override public Message save(Message message) { return repository.save(new MessageEntity(message)).toDomain(); }
    @Override public List<Message> findByChatId(UUID chatId, int limit) { return repository.findTop100ByChatIdOrderByCreatedAtAsc(chatId).stream().limit(limit).map(MessageEntity::toDomain).toList(); }
}