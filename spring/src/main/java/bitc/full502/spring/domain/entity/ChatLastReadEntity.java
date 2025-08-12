package bitc.full502.spring.domain.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@Entity
@Table(
        name = "chat_last_read",
        uniqueConstraints = @UniqueConstraint(name="uk_last_read_room_user", columnNames = {"room_id","user_id"}),
        indexes = {
                @Index(name="idx_last_read_room", columnList = "room_id"),
                @Index(name="idx_last_read_user", columnList = "user_id")
        }
)
public class ChatLastReadEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="room_id", nullable=false, length=100)
    private String roomId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="user_id", nullable=false) // users.id FK
    private Users user;

    @Column(name="last_read_at", nullable=false)
    private Instant lastReadAt;

    @PrePersist
    public void prePersist() {
        if (lastReadAt == null) lastReadAt = Instant.EPOCH; // 기본값(아무 것도 안 읽음)
    }
}
