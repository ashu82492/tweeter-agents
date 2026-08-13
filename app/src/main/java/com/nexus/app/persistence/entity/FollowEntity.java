package com.nexus.app.persistence.entity;

import com.nexus.identity.domain.Follow;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.sql.Types;
import java.time.Instant;
import java.util.UUID;
import org.hibernate.annotations.JdbcTypeCode;

@Entity
@Table(name = "follows")
public class FollowEntity {
    @Id @JdbcTypeCode(Types.CHAR) private UUID id;
    @Column(name = "follower_id", nullable = false) @JdbcTypeCode(Types.CHAR) private UUID followerId;
    @Column(name = "followee_id", nullable = false) @JdbcTypeCode(Types.CHAR) private UUID followeeId;
    @Column(name = "created_at", nullable = false) private Instant createdAt;

    protected FollowEntity() { }
    public FollowEntity(Follow follow) { id = follow.id(); followerId = follow.followerId(); followeeId = follow.followeeId(); createdAt = follow.createdAt(); }
    public Follow toDomain() { return new Follow(id, followerId, followeeId, createdAt); }
}