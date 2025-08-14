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
import org.springframework.web.bind.MissingServletRequestParameterException;
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

//    // 방 히스토리
//    @GetMapping("/history")
//    @Transactional(readOnly = true)
//    public List<ChatMessageDTO> history(@RequestParam String roomId,
//                                        @RequestParam String me,                            // 내 아이디
//                                        @RequestParam(required = false) String other,       // 상대 (옵션)
//                                        @RequestParam(defaultValue = "50") int size,
//                                        @RequestParam(required = false) Long beforeId) {
//
//        if (size <= 0) size = 50;
//        if (size > 200) size = 200;
//
//        // ✅ other 없으면 서비스로 상대 추정 (정적 호출 X)
//        if (other == null || other.isBlank()) {
//            other = chatMessageService.findPartnerId(roomId, me);
//        }
//
//        // ✅ 서비스가 ASC 정렬 + readByOther 계산까지 처리
//        return chatMessageService.history(roomId, size, beforeId, me, other);
//    }

    @GetMapping("/history")
    @Transactional(readOnly = true)
    public List<ChatMessageDTO> history(
            @RequestParam String roomId,

            // ✅ me 또는 userId 둘 다 허용
            @RequestParam(name = "userId", required = false) String userId,
            @RequestParam(name = "me",     required = false) String me,

            // ✅ 상대는 옵션 (없으면 서비스에서 추정)
            @RequestParam(name = "other",  required = false) String other,

            // ✅ size 기본 50, 1~200 사이로 클램핑
            @RequestParam(name = "size",   defaultValue = "50") int size,

            // ✅ beforeId는 null 또는 1 이상만 허용
            @RequestParam(name = "beforeId", required = false) Long beforeId
    ) throws MissingServletRequestParameterException {

        // 1) 호출자 id 확정
        String caller = (userId != null && !userId.isBlank()) ? userId : me;
        if (caller == null || caller.isBlank()) {
            throw new MissingServletRequestParameterException("userId (or me)", "String");
        }

        // 2) size/beforeId 안전화
        if (size < 1)   size = 50;
        if (size > 200) size = 200;
        if (beforeId != null && beforeId < 1) beforeId = null;

        // 3) 상대 미지정이면 서비스에서 파트너 추정
        if (other == null || other.isBlank()) {
            other = chatMessageService.findPartnerId(roomId, caller);
        }

        // 4) 서비스에서 ASC 정렬 + readByOther 계산까지 처리 (너가 쓰던 방식 유지)
        return chatMessageService.history(roomId, size, beforeId, caller, other);
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
