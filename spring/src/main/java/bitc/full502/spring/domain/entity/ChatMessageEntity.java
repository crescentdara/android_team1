package bitc.full502.spring.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;

<<<<<<< HEAD
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
=======
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
>>>>>>> testmerge/LodgingMerge
@Entity
@Table(name = "chat_message")
public class ChatMessageEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
<<<<<<< HEAD

    @Column(nullable = false, length = 100)
    private String roomId;

    @Column(nullable = false, length = 100)
    private String senderId;

=======
    @Column(nullable = false, length = 100)
    private String roomId;
    @Column(nullable = false, length = 100)
    private String senderId;
>>>>>>> testmerge/LodgingMerge
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private MessageType type;
    @Column(nullable = false, length = 2000)
    private String content;
<<<<<<< HEAD

=======
>>>>>>> testmerge/LodgingMerge
    @Column(nullable = false)
    private Instant sentAt;
    @Column(length = 100)
    private String receiverId;

    @Column(length = 100)
    private String receiverId;

    @PrePersist
    public void prePersist() {
        if (type == null) type = MessageType.TEXT;
        if (sentAt == null) sentAt = Instant.now();
    }
    public enum MessageType {TEXT, JOIN, LEAVE}
}