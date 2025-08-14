package bitc.full502.spring.dto;

import jakarta.annotation.Nullable;
import jakarta.persistence.Column;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.antlr.v4.runtime.misc.NotNull;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookingRequestDto {
    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    private Long flId;

    @Column(nullable = false)
    private Integer seatCnt = 1;

    @Column(nullable = false)
    private Integer adult = 1;

    private Integer child = 0;

    @Column(nullable = false)
    private String dep;

    @Column(nullable = false)
    private String arr;

    @Column(nullable = false)
    private LocalDate departureDate;

    // 편도 선택 시 returnDate 없을 수 있으니 null 가능
    private LocalDate returnDate;

    @Column(nullable = false)
    private Long totalPrice;
}
