package bitc.full502.spring.domain.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalTime;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@Entity @Table(name = "flight")
public class Flight {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 20)
    private String type;

    @Column(name = "fl_no", length = 20, nullable = false)
    private String flNo;

    @Column(length = 50)
    private String airline;

    @Column(length = 10)
    private String dep;

    @Column(name = "dep_time")
    private LocalTime depTime;

    @Column(length = 10)
    private String arr;

    @Column(name = "arr_time")
    private LocalTime arrTime;

    @Column(length = 40)
    private String days;

    @Column(name = "total_seat")
    private Integer totalSeat = 20;

}
