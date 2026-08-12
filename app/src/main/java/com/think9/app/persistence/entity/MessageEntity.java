package com.think9.app.persistence.entity;

import com.think9.messaging.domain.Message;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.sql.Types;
import java.time.Instant;
import java.util.UUID;
import org.hibernate.annotations.JdbcTypeCode;

@Entity
@Table(name = "messages")
public class MessageEntity {
    @Id @JdbcTypeCode(Types.CHAR) private UUID id;
    @Column(name = "chat_id", nullable = false) @JdbcTypeCode(Types.CHAR) private UUID chatId;
    @Column(name = "sender_id", nullable = false) @JdbcTypeCode(Types.CHAR) private UUID senderId;
    @Column(nullable = false, length = 4000) private String content;
    @Column(name = "idempotency_key", nullable = false, length = 128) private String idempotencyKey;
    @Column(name = "created_at", nullable = false) private Instant createdAt;

    protected MessageEntity() { }
    public MessageEntity(Message message) { id = message.id(); chatId = message.chatId(); senderId = message.senderId(); content = message.content(); idempotencyKey = message.idempotencyKey(); createdAt = message.createdAt(); }
    public Message toDomain() { return new Message(id, chatId, senderId, content, idempotencyKey, createdAt); }
}