package bitc.full502.spring.domain.entity;

import jakarta.persistence.*;
import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@Entity
@Table(name = "lod_wish",
        uniqueConstraints = @UniqueConstraint(name = "uk_lod_wish_user_lod", columnNames = {"user_id","lod_id"}))
public class LodWish {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "user_id", nullable = false)
    private Users user;

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "lod_id", nullable = false)
    private Lodging lodging;
}
