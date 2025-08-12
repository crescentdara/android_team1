package bitc.full502.spring.domain.repository;

import bitc.full502.spring.domain.entity.ChatLastReadEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ChatLastReadRepository extends JpaRepository<ChatLastReadEntity, Long> {
    Optional<ChatLastReadEntity> findByRoomIdAndUserId(String roomId, String userId);
}

