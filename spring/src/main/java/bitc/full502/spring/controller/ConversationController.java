package bitc.full502.spring.controller;

import bitc.full502.spring.domain.entity.ChatLastReadEntity;
import bitc.full502.spring.domain.entity.ChatMessageEntity;
import bitc.full502.spring.domain.repository.ChatLastReadRepository;
import bitc.full502.spring.domain.repository.ChatMessageRepository;
import bitc.full502.spring.dto.ChatMessageDTO;
import bitc.full502.spring.dto.ConversationSummaryDTO;
import bitc.full502.spring.service.ChatConversationService;
import bitc.full502.spring.service.ChatMessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.Collections;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/chat")
public class ConversationController {

    private final ChatMessageService chatMessageService;
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
    @Transactional(readOnly = true)
    public List<ChatMessageDTO> history(@RequestParam String roomId,
                                        @RequestParam String me,                            // 내 아이디
                                        @RequestParam(required = false) String other,       // 상대 (옵션)
                                        @RequestParam(defaultValue = "50") int size,
                                        @RequestParam(required = false) Long beforeId) {

        if (size <= 0) size = 50;
        if (size > 200) size = 200;

        // ✅ other 없으면 서비스로 상대 추정 (정적 호출 X)
        if (other == null || other.isBlank()) {
            other = chatMessageService.findPartnerId(roomId, me);
        }

        // ✅ 서비스가 ASC 정렬 + readByOther 계산까지 처리
        return chatMessageService.history(roomId, size, beforeId, me, other);
    }

//    // 읽음 처리
//    @PutMapping("/read")
//    @ResponseStatus(HttpStatus.NO_CONTENT)
//    public void markRead(@RequestParam String roomId, @RequestParam String userId) {
//        ChatLastReadEntity e = lastReadRepo.findByRoomIdAndUserId(roomId, userId)
//                .orElseGet(() -> ChatLastReadEntity.builder()
//                        .roomId(roomId).userId(userId).build());
//        e.setLastReadAt(Instant.now());
//        lastReadRepo.save(e);
//    }

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
