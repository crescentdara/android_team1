package bitc.full502.spring.controller;

import bitc.full502.spring.dto.ConversationSummaryDTO;
import bitc.full502.spring.service.ChatConversationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/chat")
public class ConversationController {

    private final ChatConversationService conversationService;

    // 홈의 "채팅 탭" 들어갈 때 호출 → 카톡식 대화 목록
    @GetMapping("/conversations")
    public List<ConversationSummaryDTO> list(@RequestParam String userId) {
        return conversationService.listConversations(userId);
    }
}
