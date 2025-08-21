package bitc.full502.spring.controller;

import bitc.full502.spring.dto.BookingRequestDto;
import bitc.full502.spring.dto.BookingResponseDto;
import bitc.full502.spring.service.FlBookService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/bookings") // ✅ booking 전용 엔드포인트
@RequiredArgsConstructor
public class FlBookController {

    private final FlBookService flBookService;

    /** 예약 생성 */
    @PostMapping("/flight")
    public ResponseEntity<BookingResponseDto> createBooking(@Validated @RequestBody BookingRequestDto req) {
        BookingResponseDto res = flBookService.createBooking(req);
        return ResponseEntity.ok(res);
    }

    /** 예약 상세 조회 */
    @GetMapping("/flight/{id}")
    public ResponseEntity<BookingResponseDto> getBooking(@PathVariable Long id) {
        return ResponseEntity.ok(flBookService.getBooking(id));
    }

    /** 특정 사용자 예약 목록 */
//    @GetMapping("/user/{userId}/flights")
//    public ResponseEntity<List<BookingResponseDto>> getBookingsByUser(@PathVariable Long userId) {
//        return ResponseEntity.ok(flBookService.getBookingsByUser(userId));
//    }

    /** 예약 취소 */
    @PostMapping("/flight/{id}/cancel")
    public ResponseEntity<Void> cancelBooking(@PathVariable Long id) {
        flBookService.cancelBooking(id);
        return ResponseEntity.noContent().build();
    }
}
