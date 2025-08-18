package bitc.full502.spring.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;

<<<<<<< HEAD
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@Entity @Table(name = "chat_last_read",
        uniqueConstraints = @UniqueConstraint(columnNames = {"room_id","user_id"}))
=======
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "chat_last_read",
        uniqueConstraints = @UniqueConstraint(columnNames = {"room_id", "user_id"}))
>>>>>>> testmerge/LodgingMerge
public class ChatLastReadEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "room_id", nullable = false, length = 100)
    private String roomId;

<<<<<<< HEAD
    @Column(name="user_id", nullable=false, length=100)
=======
    @Column(name = "user_id", nullable = false, length = 100)
>>>>>>> testmerge/LodgingMerge
    private String userId;

    @Column(name = "last_read_at", nullable = false)
    private Instant lastReadAt;

    @PrePersist
    public void prePersist() {
        if (lastReadAt == null) lastReadAt = Instant.EPOCH.plusSeconds(1); // 1970-01-01 00:00:01
    }
<<<<<<< HEAD

    @Column(name="last_read_id", nullable=false)
    private Long lastReadId = 0L;

}
=======
}
>>>>>>> testmerge/LodgingMerge
