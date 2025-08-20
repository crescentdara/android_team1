package bitc.full502.spring.dto;

import lombok.*;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class BookingResponseDto {
    private Long bookingId;
    private Long userId;

    private Long outFlightId;
    private Long inFlightId;      // 왕복만

    private Integer seatCnt;
    private Integer adult;
    private Integer child;

    private Long totalPrice;
    private String status;

    private LocalDate depDate;
    private LocalDate retDate;    // 왕복만

    // ✅ 추가: 항공편 상세 정보 (가는 편)
    private String outFlNo;   // 편명
    private String outDep;    // 출발지
    private String outArr;    // 도착지

    // ✅ 추가: 항공편 상세 정보 (오는 편)
    private String inFlNo;    // 편명
    private String inDep;     // 출발지
    private String inArr;     // 도착지
}
