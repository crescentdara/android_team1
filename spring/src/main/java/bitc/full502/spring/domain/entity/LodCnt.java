package bitc.full502.spring.domain.entity;

import jakarta.persistence.*;
import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@Entity @Table(name = "lod_cnt")
public class LodCnt {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "lod_id", nullable = false)
    private Lodging lodging;

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "lod_bk_id", nullable = false)
    private LodBook lodBook;

    /** 이 예약이 점유하는 객실 수(기본 1) */
    @Column(nullable = false)
    private Integer rooms = 1;
}
