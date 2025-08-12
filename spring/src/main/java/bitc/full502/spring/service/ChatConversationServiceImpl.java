package bitc.full502.spring.service;

import bitc.full502.spring.domain.entity.ChatLastReadEntity;
import bitc.full502.spring.domain.repository.ChatLastReadRepository;
import bitc.full502.spring.domain.repository.ChatMessageRepository;
import bitc.full502.spring.dto.ConversationSummaryDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ChatConversationServiceImpl implements ChatConversationService {

    private final ChatMessageRepository messageRepo;
    private final ChatLastReadRepository lastReadRepo;

    @Override
    public List<ConversationSummaryDTO> listConversations(String userId) {
        var rows = messageRepo.findLatestConversationsByUser(userId);
        List<ConversationSummaryDTO> list = new ArrayList<>(rows.size());

        for (Object[] r : rows) {
            String partnerId = (String) r[0];
            String roomId    = (String) r[1];
            String content   = (String) r[2];
            Instant lastAt   = ((Timestamp) r[3]).toInstant();

            Instant lastReadAt = lastReadRepo.findByRoomIdAndUserId(roomId, userId)
                    .map(ChatLastReadEntity::getLastReadAt)
                    .orElse(Instant.EPOCH);

            long unread = messageRepo.countByRoomIdAndSentAtAfterAndSenderIdNot(roomId, lastReadAt, userId);

            list.add(ConversationSummaryDTO.builder()
                    .partnerId(partnerId)
                    .roomId(roomId)
                    .lastContent(content)
                    .lastAt(lastAt)
                    .unreadCount(unread)
                    .build());
        }
        return list;
    }
}
