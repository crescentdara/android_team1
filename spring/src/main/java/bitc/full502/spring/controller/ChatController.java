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
        // 1) 저장(서버에서 sentAt/type 보정)
        ChatMessageDTO saved = chatMessageService.save(message);
        log.info("saved chat: room={}, from={}, to={}, content={}",
                saved.getRoomId(), saved.getSenderId(), saved.getReceiverId(), saved.getContent());

        // 2) 방 전체 브로드캐스트(옵션이지만 유지 권장)
        String roomDest = "/topic/room." + saved.getRoomId();
        messagingTemplate.convertAndSend(roomDest, saved);

        // 3) 인박스(개인함) 전송 — 보낸 사람 에코는 항상
        if (hasText(saved.getSenderId())) {
            messagingTemplate.convertAndSendToUser(saved.getSenderId(), "/queue/inbox", saved);
        }
        //    받는 사람이 있으면 그리고 본인이 아니면 전송
        if (hasText(saved.getReceiverId()) && !saved.getSenderId().equals(saved.getReceiverId())) {
            messagingTemplate.convertAndSendToUser(saved.getReceiverId(), "/queue/inbox", saved);
        }
    }
    private boolean hasText(String s) { return s != null && !s.isBlank(); }

}