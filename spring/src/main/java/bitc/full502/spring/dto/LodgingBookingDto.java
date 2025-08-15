package bitc.full502.spring.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LodgingBookingDto {
    private int adult;
    private int child;
    private String ckIn;
    private String ckOut;
    private Long lodId;
    private String roomType;
    private String status;
    private Long totalPrice;
    private Long userId;
}

