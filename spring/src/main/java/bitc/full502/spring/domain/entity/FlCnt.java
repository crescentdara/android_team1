package bitc.full502.spring.domain.entity;

import jakarta.persistence.*;
import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@Entity @Table(name = "fl_cnt")
public class FlCnt {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "fl_id", nullable = false)
    private Flight flight;

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "fl_bk_id", nullable = false)
    private FlBook flBook;
}
