package bitc.full502.spring.service;

import bitc.full502.spring.domain.entity.ChatMessageEntity;
import bitc.full502.spring.domain.repository.ChatMessageRepository;
import bitc.full502.spring.dto.ChatMessageDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ChatMessageServiceImpl implements ChatMessageService {

    private final ChatMessageRepository repo;

    @Override
    public ChatMessageDTO save(ChatMessageDTO dto) {
        ChatMessageEntity e = ChatMessageEntity.builder()
                .roomId(dto.getRoomId())
                .senderId(dto.getSenderId())
                .receiverId(dto.getReceiverId())
                .content(dto.getContent())
                .type(dto.getType() != null
                        ? ChatMessageEntity.MessageType.valueOf(dto.getType().name())
                        : ChatMessageEntity.MessageType.TEXT)
                .sentAt(dto.getSentAt() != null ? dto.getSentAt() : Instant.now())
                .build();

        ChatMessageEntity saved = repo.save(e);
        return toDto(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ChatMessageDTO> getRecentByRoom(String roomId, int size) {
        if (size <= 0) size = 50;
        if (size > 200) size = 200;

        var page = repo.findByRoomIdOrderBySentAtDesc(roomId, PageRequest.of(0, size));
        List<ChatMessageEntity> list = page.getContent();

        // 오래된 것부터 보내기 위해 역순 정렬
        Collections.reverse(list);

        List<ChatMessageDTO> result = new ArrayList<>(list.size());
        for (ChatMessageEntity e : list) result.add(toDto(e));
        return result;
    }

    private ChatMessageDTO toDto(ChatMessageEntity e) {
        return ChatMessageDTO.builder()
                .roomId(e.getRoomId())
                .senderId(e.getSenderId())
                .receiverId(e.getReceiverId())
                .content(e.getContent())
                .type(ChatMessageDTO.MessageType.valueOf(e.getType().name()))
                .sentAt(e.getSentAt())
                .build();
    }
}