package bitc.full502.spring.domain.entity;

import jakarta.persistence.*;
import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@Entity
@Table(name = "fl_wish",
        uniqueConstraints = @UniqueConstraint(name = "uk_fl_wish_user_flight", columnNames = {"user_id","fl_id"}))
public class FlWish {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "user_id", nullable = false)
    private Users user;

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "fl_id", nullable = false)
    private Flight flight;
}
