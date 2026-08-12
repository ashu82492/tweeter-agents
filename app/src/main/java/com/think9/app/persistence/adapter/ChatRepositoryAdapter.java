package com.think9.app.persistence.adapter;

import com.think9.app.persistence.entity.ChatEntity;
import com.think9.app.persistence.jpa.ChatJpaRepository;
import com.think9.messaging.domain.Chat;
import com.think9.messaging.repository.ChatRepository;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Repository;

@Repository
public class ChatRepositoryAdapter implements ChatRepository {
    private final ChatJpaRepository repository;
    public ChatRepositoryAdapter(ChatJpaRepository repository) { this.repository = repository; }
    @Override public Optional<Chat> findByParticipantPairKey(String key) { return repository.findByParticipantPairKey(key).map(ChatEntity::toDomain); }
    @Override public Optional<Chat> findById(UUID chatId) { return repository.findById(chatId).map(ChatEntity::toDomain); }
    @Override public Chat save(Chat chat) { return repository.save(new ChatEntity(chat)).toDomain(); }
}