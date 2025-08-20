package bitc.full502.spring.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.annotation.Nullable;
import jakarta.persistence.Column;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.antlr.v4.runtime.misc.NotNull;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookingRequestDto {

    private Long id;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    private Long flId;

    @Column(nullable = false)
    private Integer seatCnt;

    @Column(nullable = false)
    private Integer adult;

    private Integer child;

    @Column(nullable = false)
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate tripDate;

    @Column(nullable = false)
    private Long totalPrice;
}
