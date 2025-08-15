package bitc.full502.spring.web;

import bitc.full502.spring.dto.AvailabilityDto;
import bitc.full502.spring.dto.LodgingDetailDto;
import bitc.full502.spring.domain.entity.Lodging;
import bitc.full502.spring.service.AvailabilityQueryService;
import bitc.full502.spring.service.LodgingService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Map;

@RestController
@RequestMapping("/api/lodging")
public class LodgingDetailController {

    private final LodgingService lodgingService;
    private final AvailabilityQueryService availabilityQueryService;

    public LodgingDetailController(LodgingService lodgingService,
                                   AvailabilityQueryService availabilityQueryService) {
        this.lodgingService = lodgingService;
        this.availabilityQueryService = availabilityQueryService;
    }

    /** 숙소 상세 조회 */
    @GetMapping("/{id}/detail")
    public ResponseEntity<LodgingDetailDto> detail(@PathVariable Long id) {
        return ResponseEntity.ok(lodgingService.getDetail(id));
    }

    /** 가용 객실(남은 수) — 싱글/디럭스/스위트 동일 재고 처리 */
    @GetMapping("/{id}/availability")
    public ResponseEntity<AvailabilityDto> availability(
            @PathVariable Long id,
            @RequestParam String checkIn,
            @RequestParam String checkOut,
            @RequestParam(required = false) Integer guests
    ) {
        LocalDate ci = LocalDate.parse(checkIn);
        LocalDate co = LocalDate.parse(checkOut);
        Lodging l = lodgingService.findByIdOrThrow(id);

        long reserved = availabilityQueryService.countOverlapping(id, ci, co);
        int total = l.getTotalRoom() == null ? 0 : l.getTotalRoom();
        int left = Math.max(total - (int) reserved, 0);

        AvailabilityDto dto = AvailabilityDto.builder()
                .available(left > 0)
                .totalRoom(total)
                .reservedRooms(reserved)
                .availableRooms(left)
                .reason(left > 0 ? null : "요청 기간 만실입니다")
                .checkIn(checkIn)
                .checkOut(checkOut)
                .guests(guests)
                .build();

        return ResponseEntity.ok(dto);
    }

    /** 결제 전 파라미터 에코 — 결제 연동 전 임시 확인 용도 */
    @PostMapping("/{id}/prepay")
    public ResponseEntity<Map<String, Object>> prepay(
            @PathVariable Long id,
            @RequestBody Map<String, Object> body
    ) {
        body.put("lodgingId", id);
        return ResponseEntity.ok(body);
    }
}
