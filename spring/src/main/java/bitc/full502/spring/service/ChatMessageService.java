package bitc.full502.spring.service;

import bitc.full502.spring.dto.ChatMessageDTO;

import java.util.List;

public interface ChatMessageService {
    ChatMessageDTO save(ChatMessageDTO dto);
    List<ChatMessageDTO> getRecentByRoom(String roomId, int size); // 최신 N개 (오래된 순으로 반환)
}
