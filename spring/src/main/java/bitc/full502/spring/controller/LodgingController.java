package bitc.full502.spring.controller;

import bitc.full502.spring.dto.AvailabilityDto;
import bitc.full502.spring.dto.LodgingDetailDto;
import bitc.full502.spring.dto.LodgingListDto;
import bitc.full502.spring.service.LodgingService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/lodgings")
public class LodgingController {

    private final LodgingService lodgingService;

    public LodgingController(LodgingService lodgingService) {
        this.lodgingService = lodgingService;
    }

    /**
     * 숙소 목록 조회 (1페이지 조건 → 2페이지 리스트)
     * - 반환: 2페이지 전용 DTO 리스트 (사진, 이름, 주소(city,town), 가격)
     */
    @GetMapping
    public List<LodgingListDto> getLodgings(
            @RequestParam(required = false) String city,
            @RequestParam(required = false) String town,
            @RequestParam(required = false) String vill,
            @RequestParam(required = false) String checkIn,
            @RequestParam(required = false) String checkOut,
            @RequestParam(required = false) Integer adults,
            @RequestParam(required = false) Integer children
    ) {
        return lodgingService.findLodgingsAsList(
                city, town, vill, checkIn, checkOut, adults, children
        );
    }

    /**
     * 예약 가능 여부 조회
     */
    @GetMapping("/{id}/availability")
    public AvailabilityDto getAvailability(
            @PathVariable Long id,
            @RequestParam String checkIn,
            @RequestParam String checkOut,
            @RequestParam(required = false) Integer guests
    ) {
        return lodgingService.checkAvailability(id, checkIn, checkOut, guests);
    }

    /**
     * 숙소 상세 조회 (조회수 증가 + 집계 포함)
     */
    @GetMapping("/{id}")
    public LodgingDetailDto getDetail(@PathVariable Long id) {
        return lodgingService.getDetail(id);
    }
}