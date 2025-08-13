package bitc.full502.spring.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.antlr.v4.runtime.misc.NotNull;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookingRequestDto {
    @NotNull
    private Long userId;

    @NotNull
    private Long flId;

    private Integer seatCnt = 1;

    private Integer adult = 1;

    private Integer child = 0;

    @NotNull
    private LocalDate tripDate;

    @NotNull
    private Long totalPrice;
}
