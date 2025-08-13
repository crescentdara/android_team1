package bitc.full502.spring.service;

import bitc.full502.spring.domain.entity.ChatLastReadEntity;
import bitc.full502.spring.domain.entity.ChatMessageEntity;
import bitc.full502.spring.domain.repository.ChatLastReadRepository;
import bitc.full502.spring.domain.repository.ChatMessageRepository;
import bitc.full502.spring.dto.ChatMessageDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class ChatMessageServiceImpl implements ChatMessageService {

    private final ChatMessageRepository msgRepo;        // ✔ 이름 통일
    private final ChatLastReadRepository lastReadRepo;

    @Override
    public ChatMessageDTO save(ChatMessageDTO dto) {
        // DTO → Entity
        ChatMessageEntity.MessageType type =
                dto.getType() != null
                        ? ChatMessageEntity.MessageType.valueOf(dto.getType().name())
                        : ChatMessageEntity.MessageType.TEXT;

        ChatMessageEntity e = ChatMessageEntity.builder()
                .roomId(dto.getRoomId())
                .senderId(dto.getSenderId())
                .receiverId(dto.getReceiverId())
                .content(dto.getContent())
                .type(type)
                .sentAt(dto.getSentAt() != null ? dto.getSentAt() : Instant.now())
                .build();

        ChatMessageEntity saved = msgRepo.save(e);

        // Entity → DTO (저장 직후 내 메시지는 기본 readByOther=false)
        ChatMessageDTO out = toDto(saved);
        out.setReadByOther(Boolean.FALSE);
        return out;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ChatMessageDTO> getRecentByRoom(String roomId, int size) {
        if (size <= 0) size = 50;
        if (size > 200) size = 200;

        // id DESC로 최신부터 받고 → 클라가 ASC 쓰므로 reverse
        Pageable p = PageRequest.of(0, size, Sort.by(Sort.Direction.DESC, "id"));
        List<ChatMessageEntity> desc = msgRepo.findByRoomIdOrderByIdDesc(roomId, p);
        Collections.reverse(desc); // ASC

        List<ChatMessageDTO> result = new ArrayList<>(desc.size());
        for (ChatMessageEntity e : desc) {
            result.add(toDto(e));
        }
        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ChatMessageDTO> history(String roomId, int size, Long beforeId, String me, String other) {
        if (size <= 0) size = 50;
        if (size > 200) size = 200;

        List<ChatMessageEntity> desc;
        if (beforeId == null) {
            // 최신 N개 (id DESC)
            Pageable p = PageRequest.of(0, size, Sort.by(Sort.Direction.DESC, "id"));
            desc = msgRepo.findByRoomIdOrderByIdDesc(roomId, p);
        } else {
            // beforeId 이전 N개 (id DESC)
            desc = msgRepo.findTopNByRoomIdAndIdLessThanOrderByIdDesc(roomId, beforeId, size);
            // ↑ 리포지토리에 없는 경우:
            //   Pageable p = PageRequest.of(0, size, Sort.by(Sort.Direction.DESC, "id"));
            //   desc = msgRepo.findByRoomIdAndIdLessThan(roomId, beforeId, p);
        }

        // ASC(과거→현재)로 변환
        Collections.reverse(desc);

        // 상대방의 읽음 포인터
        long otherLastReadId = lastReadRepo.findByRoomIdAndUserId(roomId, other)
                .map(ChatLastReadEntity::getLastReadId)
                .orElse(0L);

        // 매핑 + 내가 보낸 메시지의 readByOther 세팅
        List<ChatMessageDTO> out = new ArrayList<>(desc.size());
        for (ChatMessageEntity m : desc) {
            ChatMessageDTO d = toDto(m);
            boolean mine = me != null && me.equals(d.getSenderId());
            boolean readByOther = mine && d.getId() != null && d.getId() <= otherLastReadId;
            d.setReadByOther(readByOther);
            out.add(d);
        }
        return out;
    }

    @Override
    @Transactional(readOnly = true)
    public long findMaxIdByRoom(String roomId) {
        return msgRepo.findMaxIdByRoom(roomId);
    }

    @Override
    @Transactional(readOnly = true)
    public String findPartnerId(String roomId, String me) {
        // 1) 최근 메시지로 먼저 추정 (1:1 방 전제)
        Optional<ChatMessageEntity> last = msgRepo.findTop1ByRoomIdOrderByIdDesc(roomId);
        if (last.isPresent()) {
            ChatMessageEntity m = last.get();
            return (me != null && me.equals(m.getSenderId())) ? m.getReceiverId() : m.getSenderId();
        }

        // 2) 메시지가 하나도 없으면 last_read에서 추정 (Java 8 스타일)
        return lastReadRepo.findTop1ByRoomIdAndUserIdNotOrderByIdDesc(roomId, me)
                .map(ChatLastReadEntity::getUserId)
                .orElseGet(() ->
                        lastReadRepo.findTop1ByRoomIdOrderByIdDesc(roomId)
                                .map(ChatLastReadEntity::getUserId)
                                .orElse(null)
                );
    }



    /* ===================== helper ===================== */

    private ChatMessageDTO toDto(ChatMessageEntity e) {
        ChatMessageDTO dto = ChatMessageDTO.builder()
                .id(e.getId()) // ✔ id 반드시 포함
                .roomId(e.getRoomId())
                .senderId(e.getSenderId())
                .receiverId(e.getReceiverId())
                .content(e.getContent())
                .type(ChatMessageDTO.MessageType.valueOf(e.getType().name()))
                .sentAt(e.getSentAt())
                .build();
        // readByOther는 history()에서 계산/세팅, save() 직후는 false로 세팅
        return dto;
    }
}
