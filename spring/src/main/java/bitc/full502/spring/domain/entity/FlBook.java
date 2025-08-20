package bitc.full502.spring.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@Entity @Table(name = "fl_book")
public class FlBook {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private Users user;

    /** 가는 편 항공편 */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fl_id", nullable = false)
    private Flight flight;

    /** 오는 편 항공편(왕복일 때만) */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "return_fl_id")
    private Flight returnFlight; // null 가능(편도)

    private Integer adult;
    private Integer child;

    @Column(name = "total_price")
    private Long totalPrice;

    @Column(length = 20)
    private String status; // e.g. PAID / CANCEL

    /** 가는 날(필수) */
    @Column(name = "dep_date", nullable = false)
    private LocalDate depDate;

    /** 오는 날(왕복일 때만) */
    @Column(name = "ret_date")
    private LocalDate retDate; // null 가능
}
