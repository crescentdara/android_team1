package bitc.full502.spring.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LodgingListDto {
    private Long id;
    private String name;
    private String city;
    private String town;
    private String addrRd;   // 도로명 주소(없으면 지번 주소로 대체 가능)
          // 노출용 대표가/최저가 (백엔드 basePrice 값 매핑)
    private String img;
    private Long basePrice;
}