package bitc.full502.spring.controller;

import bitc.full502.spring.domain.entity.ChatLastReadEntity;
import bitc.full502.spring.domain.repository.ChatLastReadRepository;
import bitc.full502.spring.dto.ChatMessageDTO;
import bitc.full502.spring.dto.ReadReceiptDTO;
import bitc.full502.spring.service.ChatMessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;

@Slf4j
@RestController // ✅ REST 응답을 바로 반환 (기존 @Controller였다면 교체 권장)
@RequiredArgsConstructor
@RequestMapping("/api/chat") // ✅ REST 경로 prefix (STOMP는 그대로 /app)
public class ChatController {

    private final SimpMessagingTemplate messagingTemplate;
    private final ChatMessageService chatMessageService;
    private final ChatLastReadRepository lastReadRepo;

    /* ===================== STOMP (클라 → /app/chat.send) ===================== */
    @MessageMapping("/chat.send")
    public void handleMessage(@Payload ChatMessageDTO message) {
        // 1) 저장 (서버에서 sentAt/type 보정)
        ChatMessageDTO saved = chatMessageService.save(message);
        log.info("saved chat: room={}, from={}, to={}, content={}",
                saved.getRoomId(), saved.getSenderId(), saved.getReceiverId(), saved.getContent());

        // 2) 방 브로드캐스트
        messagingTemplate.convertAndSend("/topic/room." + saved.getRoomId(), saved);

        // 3) 개인 에코 (보낸 사람/받는 사람)
        if (hasText(saved.getSenderId())) {
            messagingTemplate.convertAndSendToUser(saved.getSenderId(), "/queue/inbox", saved);
        }
        if (hasText(saved.getReceiverId()) && !saved.getSenderId().equals(saved.getReceiverId())) {
            messagingTemplate.convertAndSendToUser(saved.getReceiverId(), "/queue/inbox", saved);
        }
    }

    /* ===================== REST: 읽음 처리 + 영수증 브로드캐스트 ===================== */

    // PUT /api/chat/read?roomId=...&userId=...
    @PutMapping("/read")
    @Transactional
    public ReadReceiptDTO markRead(@RequestParam String roomId, @RequestParam String userId) {
        // 1) 방의 최신 메시지 id
        long maxId = chatMessageService.findMaxIdByRoom(roomId);
        Instant now = Instant.now();

        // 2) 내 last_read upsert (이 값이 나중에 readByOther 계산에 쓰임)
        ChatLastReadEntity e = lastReadRepo.findByRoomIdAndUserId(roomId, userId)
                .orElseGet(() -> ChatLastReadEntity.builder()
                        .roomId(roomId)
                        .userId(userId)
                        .lastReadId(0L)
                        .build());

        // 역주행 방지: 더 큰 값만 반영
        if (maxId > e.getLastReadId()) {
            e.setLastReadId(maxId);
            e.setLastReadAt(now);
            lastReadRepo.save(e);
        }

        // 3) 상대(1:1 전제)에게 보낼 영수증
        ReadReceiptDTO dto = new ReadReceiptDTO(roomId, userId, e.getLastReadId(), now);

        // 4) STOMP 브로드캐스트: 나(에코) + 상대 둘 다
        messagingTemplate.convertAndSendToUser(userId, "/queue/read-receipt", dto);
        String partnerId = chatMessageService.findPartnerId(roomId, userId);
        if (partnerId != null && !partnerId.isBlank() && !partnerId.equals(userId)) {
            messagingTemplate.convertAndSendToUser(partnerId, "/queue/read-receipt", dto);
        }

        return dto;
    }


    /* ===================== 유틸 ===================== */
    private boolean hasText(String s) { return s != null && !s.isBlank(); }
}
