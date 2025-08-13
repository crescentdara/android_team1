package bitc.full502.spring.domain.repository;

import bitc.full502.spring.domain.entity.ChatMessageEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;

public interface ChatMessageRepository extends JpaRepository<ChatMessageEntity, Long> {

    // 최신 N개 (내림차순)
    List<ChatMessageEntity> findTop50ByRoomIdOrderBySentAtDesc(String roomId);

    // 최신 N개(페이지) — 히스토리 불러올 때 사용
    Page<ChatMessageEntity> findByRoomIdOrderBySentAtDesc(String roomId, Pageable pageable);

    // 미확인 카운트
    long countByRoomIdAndSentAtAfterAndSenderIdNot(String roomId, Instant lastReadAt, String currentUserId);

    // (대화 목록용) 사용자 기준 최신 대화 상대 1건씩 뽑기 — MySQL 8 윈도우 함수 사용
    @Query(value = """
    SELECT partner_id, room_id, last_content, last_at FROM (
        SELECT
            CASE WHEN m.sender_id = :userId THEN m.receiver_id ELSE m.sender_id END AS partner_id,
            m.room_id  AS room_id,
            m.content  AS last_content,
            m.sent_at  AS last_at,
            ROW_NUMBER() OVER (
                PARTITION BY CASE WHEN m.sender_id = :userId THEN m.receiver_id ELSE m.sender_id END
                ORDER BY m.sent_at DESC
            ) rn
        FROM chat_message m
        WHERE m.sender_id = :userId OR m.receiver_id = :userId
        ) t

        WHERE t.rn = 1
        ORDER BY t.last_at DESC
        """, nativeQuery = true
    )
    List<Object[]> findLatestConversationsByUser(@Param("userId") String userId);

    Page<ChatMessageEntity> findByRoomId(String roomId, Pageable pageable);
}
