package bitc.full502.spring.dto;


import jakarta.persistence.Column;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookingRequestDto {

    private Long id;

    @Column(nullable = false)
    private Long userId;

    // 가는편
    private Long outFlId;         // 기존 flId → outFlId 로 명확히
    private LocalDate depDate;    // 가는 날(yyyy-MM-dd)

    // 오는편(왕복이면 채움)
    private Long inFlId;          // 왕복일 경우만
    private LocalDate retDate;    // 왕복일 경우만

    private Integer seatCnt;      // 없으면 adult+child
    private Integer adult = 1;
    private Integer child = 0;

    private Long totalPrice;      // 합계 결제 금액
}
