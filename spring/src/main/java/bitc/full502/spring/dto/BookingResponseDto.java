package bitc.full502.spring.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookingResponseDto {
    private Long bookingId;
    private Long userId;
    private Long flightId;
    private Integer seatCnt;
    private Integer adult;
    private Integer child;
    private Long totalPrice;
    private String status;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate tripDate;
}
