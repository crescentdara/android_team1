package bitc.full502.spring.domain.entity;

import jakarta.persistence.*;
import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@Entity @Table(name = "lodging")
public class Lodging {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 40)
    private String city;

    @Column(length = 40)
    private String town;

    @Column(length = 40)
    private String vill;

    @Column(length = 100, nullable = false)
    private String name;

    @Column(length = 30)
    private String phone;

    @Column(name = "addr_rd", length = 200)
    private String addrRd;

    @Column(name = "addr_jb", length = 200)
    private String addrJb;

    @Column
    private Double lat;

    @Column
    private Double lon;

    @Column(name = "total_room")
    private Integer totalRoom =3;

    @Column(length = 255)
    private String img;
    /** 2페이지 노출용 대표가/최저가 (추가 필드, ddl-auto=update로 컬럼 자동 추가) */
    @Column(name = "base_price", nullable = false)
    private Integer basePrice = 100000;
}