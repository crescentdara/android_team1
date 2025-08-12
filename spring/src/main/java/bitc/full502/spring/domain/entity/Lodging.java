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
}
