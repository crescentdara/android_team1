package bitc.full502.spring.domain.repository;

import bitc.full502.spring.domain.entity.ChatMessageEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface ChatMessageRepository extends JpaRepository<ChatMessageEntity, Long> {

    // 최신 N개 (내림차순)
    List<ChatMessageEntity> findTop50ByRoomIdOrderBySentAtDesc(String roomId);

    // 첫 페이지: 최근 메시지 size개 (id 내림차순)
    List<ChatMessageEntity> findByRoomIdOrderByIdDesc(String roomId, Pageable pageable);

    // 이전 페이지: beforeId 미만만 (엄격히 '<') size개 (id 내림차순)
    List<ChatMessageEntity> findByRoomIdAndIdLessThanOrderByIdDesc(String roomId, Long beforeId, Pageable pageable);

    // 미확인 카운트
    long countByRoomIdAndSentAtAfterAndSenderIdNot(String roomId, Instant lastReadAt, String currentUserId);

    // (대화 목록용) 사용자 기준 최신 대화 상대 1건씩 뽑기 — MySQL 8 윈도우 함수 사용
    @Query(value = """
        SELECT
          CASE WHEN m.sender_id = :userId THEN m.receiver_id ELSE m.sender_id END AS partner_id,
          m.room_id                                   AS room_id,
          m.content                                   AS last_content,
          m.sent_at                                   AS last_at,
          m.id                                        AS last_id
        FROM chat_message m
        JOIN (
          SELECT room_id, MAX(id) AS max_id
          FROM chat_message
          WHERE sender_id = :userId OR receiver_id = :userId
          GROUP BY room_id
        ) x ON x.room_id = m.room_id AND x.max_id = m.id
        ORDER BY m.id DESC
        """, nativeQuery = true)
    List<Object[]> findLatestConversationsByUser(@Param("userId") String userId);


    // 읽지 않은 메세지 관련 쿼리
    Page<ChatMessageEntity> findByRoomId(String roomId, Pageable pageable);

    @Query(value = "SELECT COALESCE(MAX(id),0) FROM chat_message WHERE room_id=:roomId", nativeQuery = true)
    long findMaxIdByRoom(@Param("roomId") String roomId);

    @Query(value = """
    SELECT COUNT(*) FROM chat_message
    WHERE room_id=:roomId AND sender_id<>:me AND id>:lastReadId
""", nativeQuery = true)
    long countUnread(@Param("roomId") String roomId, @Param("me") String me, @Param("lastReadId") long lastReadId);

    // 미읽음 계산용: 내 마지막 읽음 id보다 큰, 상대가 보낸 메시지 수
    long countByRoomIdAndIdGreaterThanAndSenderIdNot(String roomId, long lastReadId, String senderIdNot);

    // beforeId 이전 N개 (DESC) - N 가변 버전
    @Query(value = "SELECT * FROM chat_message WHERE room_id = :roomId AND id < :beforeId ORDER BY id DESC LIMIT :size", nativeQuery = true)
    List<ChatMessageEntity> findTopNByRoomIdAndIdLessThanOrderByIdDesc(@Param("roomId") String roomId,
                                                                       @Param("beforeId") Long beforeId,
                                                                       @Param("size") int size);

    Optional<ChatMessageEntity> findTop1ByRoomIdOrderByIdDesc(String roomId);

}
