//package bitc.full502.spring.dto;
//
//import com.fasterxml.jackson.annotation.JsonFormat;
//import jakarta.annotation.Nullable;
//import jakarta.persistence.Column;
//import lombok.AllArgsConstructor;
//import lombok.Data;
//import lombok.NoArgsConstructor;
//import org.antlr.v4.runtime.misc.NotNull;
//
//import java.time.LocalDate;
//
//@Data
//@NoArgsConstructor
//@AllArgsConstructor
//public class BookingRequestDto {
//    @Column(nullable = false)
//    private Long userId;
//
//    @Column(nullable = false)
//    private Long flId;
//
//    @Column(nullable = false)
//    private Integer seatCnt;
//
//    @Column(nullable = false)
//    private Integer adult;
//
//    private Integer child;
//
//    @Column(nullable = false)
//    @JsonFormat(pattern = "yyyy-MM-dd")
//    private LocalDate tripDate;
//
//    @Column(nullable = false)
//    private Long totalPrice;
//}

package bitc.full502.spring.dto;

import lombok.*;
import java.time.LocalDate;

@Data @NoArgsConstructor @AllArgsConstructor
public class BookingRequestDto {
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
