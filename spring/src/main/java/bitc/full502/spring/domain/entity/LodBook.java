package bitc.full502.spring.domain.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@Entity @Table(name = "lod_book")
public class LodBook {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "user_id", nullable = false)
    private Users user;

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "lod_id", nullable = false)
    private Lodging lodging;

    @Column(name = "ck_in")
    private LocalDate ckIn;

    @Column(name = "ck_out")
    private LocalDate ckOut;

    private Integer adult;
    private Integer child;

    @Column(name = "room_type", length = 50)
    private String roomType;

    @Column(name = "total_price")
    private long totalPrice;

    @Column(length = 20)
    private String status;
}
