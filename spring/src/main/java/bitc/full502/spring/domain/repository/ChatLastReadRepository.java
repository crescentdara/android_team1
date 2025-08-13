package bitc.full502.spring.domain.repository;

import bitc.full502.spring.domain.entity.ChatLastReadEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ChatLastReadRepository extends JpaRepository<ChatLastReadEntity, Long> {

    Optional<ChatLastReadEntity> findByRoomIdAndUserId(String roomId, String userId);

    // ✅ 나(me)를 제외하고 해당 방에서 최근 1명
    Optional<ChatLastReadEntity> findTop1ByRoomIdAndUserIdNotOrderByIdDesc(String roomId, String userId);

    // ✅ 해당 방에서 아무나 1명 (최근)
    Optional<ChatLastReadEntity> findTop1ByRoomIdOrderByIdDesc(String roomId);
}



