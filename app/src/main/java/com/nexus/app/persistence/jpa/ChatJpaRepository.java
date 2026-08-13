package com.nexus.app.persistence.jpa;

import com.nexus.app.persistence.entity.ChatEntity;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChatJpaRepository extends JpaRepository<ChatEntity, UUID> {
    Optional<ChatEntity> findByParticipantPairKey(String participantPairKey);
}