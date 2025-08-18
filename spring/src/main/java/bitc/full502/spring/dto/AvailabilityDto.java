package bitc.full502.spring.dto;

import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class AvailabilityDto {
    private boolean available;     // 예약 가능 여부
    private int totalRoom;         // 총 객실 수
    private long reservedRooms;    // 해당 기간 이미 예약 잡힌 객실 수
    private int availableRooms;    // 남은 객실 수
    private String reason;         // 불가 사유(유효성/만실 등)
    private String checkIn;        // 에코백(요청 값)
    private String checkOut;       // 에코백
    private Integer guests;        // 에코백
}