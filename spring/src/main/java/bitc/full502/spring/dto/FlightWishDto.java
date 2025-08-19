package bitc.full502.spring.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data @NoArgsConstructor @AllArgsConstructor
public class FlightWishDto {
    private Long id;         // wish id
    private String airline;
    private String flightNo;
    private String depart;
    private String arrive;
    private String thumb;    // 필요 없으면 null/"" 반환
}
