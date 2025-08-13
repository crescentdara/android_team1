package bitc.full502.spring.service;

import bitc.full502.spring.dto.ChatMessageDTO;

import java.util.List;

public interface ChatMessageService {
    ChatMessageDTO save(ChatMessageDTO dto);

    List<ChatMessageDTO> getRecentByRoom(String roomId, int size); // 최신 N개 (오래된 순으로 반환)

    /** 히스토리: id ASC(과거→현재)로 반환하고, 내가 보낸 메시지의 readByOther를 채워서 돌려줌 */
    List<ChatMessageDTO> history(String roomId, int size, Long beforeId, String me, String other);

    /** 해당 방의 가장 큰(최근) 메시지 id. 없으면 0 */
    long findMaxIdByRoom(String roomId);

    /** 1:1 방에서 내 상대 아이디 */
    String findPartnerId(String roomId, String me);
}
