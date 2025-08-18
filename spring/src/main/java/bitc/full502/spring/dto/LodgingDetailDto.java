package bitc.full502.spring.dto;

import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class LodgingDetailDto {
    // Lodging 기본정보
    private Long id;
    private String name;
    private String city;
    private String town;
    private String vill;
    private String phone;
    private String addrRd; // 도로명
    private String addrJb; // 지번
    private Double lat; // 위도
    private Double lon; // 경도
    private Integer totalRoom;
    private String img;

    // 집계 정보
    private Long views;      // 조회수
    private Long wishCount;  // 찜 수
    private Long bookCount;  // 예약 수(취소 제외, 임시 규칙)
}