package com.think9.agent.profile;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.hibernate.annotations.JdbcTypeCode;

import java.sql.Types;

@Entity
@Table(name = "agent_profiles")
public class AgentProfile {
    @Id
    @JdbcTypeCode(Types.VARCHAR)
    private UUID id;
    @Column(name = "user_id", nullable = false, unique = true, length = 36)
    @JdbcTypeCode(Types.VARCHAR)
    private UUID userId;
    @Column(nullable = false, unique = true, length = 64)
    private String username;
    @Column(name = "display_name", nullable = false, length = 100)
    private String displayName;
    @Column(nullable = false, length = 100)
    private String personality;
    @Column(nullable = false, length = 1000)
    private String interests;
    @Column(name = "encrypted_password", nullable = false, length = 1000)
    private String encryptedPassword;
    @Column(name = "password_iv", nullable = false, length = 100)
    private String passwordIv;
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;
    @Column(name = "next_action_at", nullable = false)
    private Instant nextActionAt;

    protected AgentProfile() { }

    public AgentProfile(UUID id, UUID userId, String username, String displayName, String personality,
                        List<String> interests, EncryptedCredential credential, Instant createdAt, Instant nextActionAt) {
        this.id = id;
        this.userId = userId;
        this.username = username;
        this.displayName = displayName;
        this.personality = personality;
        this.interests = String.join(",", interests);
        this.encryptedPassword = credential.ciphertext();
        this.passwordIv = credential.initializationVector();
        this.createdAt = createdAt;
        this.nextActionAt = nextActionAt;
    }

    public UUID getId() { return id; }
    public UUID getUserId() { return userId; }
    public String getUsername() { return username; }
    public String getDisplayName() { return displayName; }
    public String getPersonality() { return personality; }
    public List<String> getInterests() { return List.of(interests.split(",")); }
    public EncryptedCredential credential() { return new EncryptedCredential(encryptedPassword, passwordIv); }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getNextActionAt() { return nextActionAt; }
    public void scheduleNextActionAt(Instant value) { this.nextActionAt = value; }
}