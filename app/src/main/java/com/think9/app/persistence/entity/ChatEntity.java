package com.think9.app.persistence.entity;

import com.think9.messaging.domain.Chat;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import java.sql.Types;
import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;
import org.hibernate.annotations.JdbcTypeCode;

@Entity
@Table(name = "chats")
public class ChatEntity {
    @Id @JdbcTypeCode(Types.CHAR) private UUID id;
    @Column(name = "participant_pair_key", nullable = false, unique = true, length = 100) private String participantPairKey;
    @Column(name = "created_at", nullable = false) private Instant createdAt;
    @ElementCollection
    @CollectionTable(name = "chat_participants", joinColumns = @JoinColumn(name = "chat_id"))
    @Column(name = "participant_id", nullable = false)
    @JdbcTypeCode(Types.CHAR)
    private Set<UUID> participantIds = new LinkedHashSet<>();

    protected ChatEntity() { }
    public ChatEntity(Chat chat) { id = chat.id(); participantPairKey = chat.participantPairKey(); createdAt = chat.createdAt(); participantIds = new LinkedHashSet<>(chat.participantIds()); }
    public Chat toDomain() { return new Chat(id, participantPairKey, participantIds, createdAt); }
}