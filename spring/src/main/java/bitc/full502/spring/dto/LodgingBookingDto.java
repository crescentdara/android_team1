package bitc.full502.spring.dto;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Data
public class LodgingBookingDto {
    private Long id;          // ← 추가: 예약 PK
    private Long userId;
    private Long lodId;
    private String ckIn;
    private String ckOut;
    private Integer adult;
    private Integer child;
    private String roomType;
    private Long totalPrice;
    private String status;

    // ↓↓↓ 목록/상세 카드에 쓸 숙소 정보 추가
    private String lodName;
    private String lodImg;
    private String addrRd;
    private String addrJb;
}


