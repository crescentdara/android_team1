package bitc.full502.spring.service;

import bitc.full502.spring.dto.ConversationSummaryDTO;

import java.util.List;

public interface ChatConversationService {
    List<ConversationSummaryDTO> listConversations(String userId);
}
