package bitc.full502.spring.controller;

import bitc.full502.spring.dto.ChatMessageDTO;
import bitc.full502.spring.service.ChatMessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Slf4j
@Controller
@RequiredArgsConstructor
public class ChatController {

    private final SimpMessagingTemplate messagingTemplate;
    private final ChatMessageService chatMessageService;

    @MessageMapping("/chat.send")
    public void handleMessage(@Payload ChatMessageDTO message) {
        // 1) DB 저장(서버에서 sentAt/type 보정)
        ChatMessageDTO saved = chatMessageService.save(message);
        log.info("saved chat: room={}, from={}, content={}",
                saved.getRoomId(), saved.getSenderId(), saved.getContent());

        // 2) 구독자에게 방송(방 전체) — 기존 유지
        String destination = "/topic/room." + saved.getRoomId();
        messagingTemplate.convertAndSend(destination, saved);

        // ✅ 추가: 받는 사람(inbox) + 보낸 사람(내 대화창 동기화용)
        messagingTemplate.convertAndSendToUser(saved.getReceiverId(), "/queue/inbox", saved);
        messagingTemplate.convertAndSendToUser(saved.getSenderId(),  "/queue/inbox", saved);

    }
}