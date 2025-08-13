package bitc.full502.spring.controller;

import bitc.full502.spring.domain.entity.ChatLastReadEntity;
import bitc.full502.spring.domain.entity.ChatMessageEntity;
import bitc.full502.spring.domain.repository.ChatLastReadRepository;
import bitc.full502.spring.domain.repository.ChatMessageRepository;
import bitc.full502.spring.dto.ChatMessageDTO;
import bitc.full502.spring.dto.ConversationSummaryDTO;
import bitc.full502.spring.service.ChatConversationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.Collections;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/chat")
public class ConversationController {

    private final ChatConversationService conversationService;
    private final ChatMessageRepository chatRepo;
    private final ChatLastReadRepository lastReadRepo;

    // 대화 목록(사람당 1줄)
    @GetMapping("/conversations")
    public List<ConversationSummaryDTO> list(@RequestParam String userId) {
        return conversationService.listConversations(userId);
    }

    // 방 히스토리
    @GetMapping("/history")
    public List<ChatMessageDTO> history(@RequestParam String roomId,
                                        @RequestParam(defaultValue = "50") int size) {

        Pageable p = PageRequest.of(0, size); // 정렬은 메서드 이름에서 처리

        Page<ChatMessageEntity> page = chatRepo.findByRoomIdOrderBySentAtDesc(roomId, p);

        // ⚠ getContent()는 unmodifiable. 복사본을 만들어서 뒤집는다.
        List<ChatMessageDTO> content = page.map(this::toDto).getContent();

        // List<ChatMessageDTO> list = page.map(this::toDto).getContent();
        List<ChatMessageDTO> list = new java.util.ArrayList<>(content);

        // Collections.reverse(list); // 오래된 → 최신 순으로 보여주기
        java.util.Collections.reverse(list);
        return list;
    }

    // 읽음 처리
    @PutMapping("/read")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void markRead(@RequestParam String roomId, @RequestParam String userId) {
        ChatLastReadEntity e = lastReadRepo.findByRoomIdAndUserId(roomId, userId)
                .orElseGet(() -> ChatLastReadEntity.builder()
                        .roomId(roomId).userId(userId).build());
        e.setLastReadAt(Instant.now());
        lastReadRepo.save(e);
    }

    // 엔티티 -> DTO 변환
    private ChatMessageDTO toDto(ChatMessageEntity e) {
        return ChatMessageDTO.builder()
                .roomId(e.getRoomId())
                .senderId(e.getSenderId())
                .receiverId(e.getReceiverId())
                .content(e.getContent())
                // 서로 다른 enum 타입일 수 있으므로 name()으로 매핑
                .type(e.getType() != null
                        ? ChatMessageDTO.MessageType.valueOf(e.getType().name())
                        : ChatMessageDTO.MessageType.TEXT)
                .sentAt(e.getSentAt())
                .build();
    }
}
