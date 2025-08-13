package bitc.full502.spring.dto;

import lombok.*;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatMessageDTO {
    private String roomId;
    private String senderId;
    private String receiverId;
    private String content;

    private Instant sentAt;
    private MessageType type;
    public enum MessageType { TEXT, JOIN, LEAVE }
}