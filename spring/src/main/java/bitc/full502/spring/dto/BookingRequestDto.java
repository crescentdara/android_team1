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
    private LocalDate tripDate;

    @Column(nullable = false)
    private Long totalPrice;
}
