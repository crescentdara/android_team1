package bitc.full502.spring.controller;

import bitc.full502.spring.domain.entity.Lodging;
import bitc.full502.spring.domain.repository.LodgingRepository;
import bitc.full502.spring.dto.AvailabilityDto;
import bitc.full502.spring.service.LodgingService;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;     // ★ 누락되어 에러났던 import
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam; // ★ 이것도 필요
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/lodgings")
public class LodgingController {

    private final LodgingRepository lodgingRepository;
    private final LodgingService lodgingService;

    // ★ 두 개 모두 주입받도록 생성자 수정
    public LodgingController(LodgingRepository lodgingRepository,
                             LodgingService lodgingService) {
        this.lodgingRepository = lodgingRepository;
        this.lodgingService = lodgingService;
    }
    /**
     * 숙소 목록 조회 API
     * - 주소: GET /api/lodgings
     * - 쿼리: page(0부터 시작), size
     * - 정렬: id 오름차순
     * - 반환: Page<Lodging> (엔티티 그대로 내려서, 목록 JSON 확인용으로 가장 단순)
     *
     * 사용 예:
     *   /api/lodgings           -> 기본 0페이지, 10개
     *   /api/lodgings?page=0&size=20
     */

    /** 숙소 목록 조회 (페이지네이션) */
    @GetMapping
    public Page<Lodging> getLodgings(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        // ★ Sort.by(Sort.Direction.ASC, "id") 형태로 정확히 입력
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "id"));
        return lodgingRepository.findAll(pageable);
    }

    /** 예약 가능 여부 */
    @GetMapping("/{id}/availability")
    public AvailabilityDto getAvailability(
            @PathVariable Long id,                 // ★ import 필요
            @RequestParam String checkIn,          // YYYY-MM-DD
            @RequestParam String checkOut,         // YYYY-MM-DD
            @RequestParam(required = false) Integer guests
    ) {
        return lodgingService.checkAvailability(id, checkIn, checkOut, guests);
    }
}