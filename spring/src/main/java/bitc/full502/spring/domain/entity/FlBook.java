package bitc.full502.spring.domain.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@Entity @Table(name = "fl_book")
public class FlBook {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "user_id", nullable = false)
    private Users user;

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "fl_id", nullable = false)
    private Flight flight;

    private Integer adult;
    private Integer child;

    @Column(name = "total_price")
    private Long totalPrice;

    @Column(length = 20)
    private String status;

    @Column(name = "trip_date")
    private LocalDate tripDate;
}
