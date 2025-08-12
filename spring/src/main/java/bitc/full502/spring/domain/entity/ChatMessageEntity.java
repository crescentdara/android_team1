package bitc.full502.spring.domain.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@Entity
@Table(
        name = "chat_message",
        indexes = {
                @Index(name="idx_chat_msg_room", columnList = "room_id"),
                @Index(name="idx_chat_msg_sent_at", columnList = "sent_at"),
                @Index(name="idx_chat_msg_sender", columnList = "sender_id"),
                @Index(name="idx_chat_msg_receiver", columnList = "receiver_id")
        }
)
public class ChatMessageEntity {

    public enum MessageType { TEXT, JOIN, LEAVE }

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="room_id", nullable = false, length = 100)
    private String roomId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id", nullable = false)  // users.id FK
    private Users sender;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "receiver_id", nullable = false) // users.id FK
    private Users receiver;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private MessageType type;

    @Column(nullable = false, length = 2000)
    private String content;

    @Column(name="sent_at", nullable = false)
    private Instant sentAt;

    @PrePersist
    public void prePersist() {
        if (type == null) type = MessageType.TEXT;
        if (sentAt == null) sentAt = Instant.now();
    }
}
