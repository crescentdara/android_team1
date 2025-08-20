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
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/chat")
public class ChatController {

    private final SimpMessagingTemplate messagingTemplate;
    private final ChatMessageService chatMessageService;
    private final ChatLastReadRepository lastReadRepo;

    /* ===================== STOMP (클라 → /app/chat.send) ===================== */
    @MessageMapping("/chat.send")
    public void handleMessage(@Payload ChatMessageDTO message) {
        // 1) 저장
        ChatMessageDTO saved = chatMessageService.save(message);
        log.info("saved chat: room={}, from={}, to={}, content={}",
                saved.getRoomId(), saved.getSenderId(), saved.getReceiverId(), saved.getContent());

        // 2) ✅ 방 토픽으로만 브로드캐스트 (클라 구독: /topic/room.{roomId})
        messagingTemplate.convertAndSend("/topic/room." + saved.getRoomId(), saved);

        // 3) ❌ 개인 큐 에코는 비활성화 (Principal 매핑 없이도 실시간 보장)
        // messagingTemplate.convertAndSendToUser(saved.getSenderId(), "/queue/inbox", saved);
        // messagingTemplate.convertAndSendToUser(saved.getReceiverId(), "/queue/inbox", saved);
    }

    /* ===================== REST: 읽음 처리 + 영수증 브로드캐스트 ===================== */
    @PutMapping("/read")
    @Transactional
    public ReadReceiptDTO markRead(@RequestParam String roomId, @RequestParam String userId) {
        long maxId = chatMessageService.findMaxIdByRoom(roomId);
        Instant now = Instant.now();

        ChatLastReadEntity e = lastReadRepo.findByRoomIdAndUserId(roomId, userId)
                .orElseGet(() -> ChatLastReadEntity.builder()
                        .roomId(roomId)
                        .userId(userId)
                        .lastReadId(0L)
                        .build());

        if (maxId > e.getLastReadId()) {
            e.setLastReadId(maxId);
            e.setLastReadAt(now);
            lastReadRepo.save(e);
        }

        ReadReceiptDTO dto = new ReadReceiptDTO(roomId, userId, e.getLastReadId(), now);

        // ✅ 읽음 영수증도 방 토픽으로 브로드캐스트 (클라 구독: /topic/room.{roomId}.read)
        messagingTemplate.convertAndSend("/topic/room." + roomId + ".read", dto);

        // ❌ 개인 큐 전송 비활성화
        // messagingTemplate.convertAndSendToUser(userId, "/queue/read-receipt", dto);
        // String partnerId = chatMessageService.findPartnerId(roomId, userId);
        // if (partnerId != null && !partnerId.isBlank() && !partnerId.equals(userId)) {
        //     messagingTemplate.convertAndSendToUser(partnerId, "/queue/read-receipt", dto);
        // }

        return dto;
    }

    private boolean hasText(String s) { return s != null && !s.isBlank(); }
}
