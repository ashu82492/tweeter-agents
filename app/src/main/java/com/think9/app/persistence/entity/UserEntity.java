package com.think9.app.persistence.entity;

import com.think9.identity.domain.User;
import com.think9.identity.domain.UserType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.sql.Types;
import java.time.Instant;
import java.util.UUID;
import org.hibernate.annotations.JdbcTypeCode;

@Entity
@Table(name = "users")
public class UserEntity {
    @Id
    @JdbcTypeCode(Types.CHAR)
    private UUID id;
    @Column(nullable = false, unique = true, length = 64)
    private String username;
    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;
    @Column(name = "display_name", nullable = false, length = 100)
    private String displayName;
    @Enumerated(EnumType.STRING)
    @Column(name = "user_type", nullable = false, length = 32)
    private UserType type;
    @Column(nullable = false)
    private boolean enabled;
    @Column(name = "last_active_at")
    private Instant lastActiveAt;
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected UserEntity() { }

    public UserEntity(User user) {
        this.id = user.id(); this.username = user.username(); this.passwordHash = user.passwordHash(); this.displayName = user.displayName();
        this.type = user.type(); this.enabled = user.enabled(); this.lastActiveAt = user.lastActiveAt(); this.createdAt = user.createdAt(); this.updatedAt = user.updatedAt();
    }

    public User toDomain() { return new User(id, username, passwordHash, displayName, type, enabled, lastActiveAt, createdAt, updatedAt); }
}