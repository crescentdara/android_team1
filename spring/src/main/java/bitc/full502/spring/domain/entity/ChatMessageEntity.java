package bitc.full502.spring.domain.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
@Entity
@Table(name = "chat_message")
public class ChatMessageEntity {

    public enum MessageType { TEXT, JOIN, LEAVE }

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String roomId;

    @Column(nullable = false, length = 100)
    private String senderId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private MessageType type;

    @Column(nullable = false, length = 2000)
    private String content;

    @Column(nullable = false)
    private Instant sentAt;

    @Column(nullable = false, length = 100)
    private String receiverId;


    @PrePersist
    public void prePersist() {
        if (type == null) type = MessageType.TEXT;
        if (sentAt == null) sentAt = Instant.now();
    }
}
