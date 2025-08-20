package bitc.full502.spring.dto;

import lombok.*;
import java.time.LocalDate;

@Data @NoArgsConstructor @AllArgsConstructor
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
}
