package bitc.full502.spring.controller;

import bitc.full502.spring.domain.entity.ChatLastReadEntity;
import bitc.full502.spring.domain.repository.ChatLastReadRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/chat")
public class ReadController {

    private final ChatLastReadRepository lastReadRepo;

    // 채팅방 화면이 열릴 때마다 호출 → 미확인 0으로
    @PutMapping("/read")
    public void markRead(@RequestParam String roomId, @RequestParam String userId) {
        var rec = lastReadRepo.findByRoomIdAndUserId(roomId, userId)
                .orElse(ChatLastReadEntity.builder().roomId(roomId).userId(userId).build());
        rec.setLastReadAt(Instant.now());
        lastReadRepo.save(rec);
    }
}