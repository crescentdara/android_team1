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
@RequestMapping("/api/bookings/flight")
@RequiredArgsConstructor
public class FlBookController {

    private final FlBookService flBookService;

    // 예약 생성
    @PostMapping
    public ResponseEntity<BookingResponseDto> createBooking(@Validated @RequestBody BookingRequestDto req) {

        BookingResponseDto res = flBookService.createBooking(req);

        return ResponseEntity.ok(res);
    }

    // 예약 상세
    @GetMapping("/{id}")
    public ResponseEntity<BookingResponseDto> getBooking(@PathVariable Long id) {
        return ResponseEntity.ok(flBookService.getBooking(id));
    }

    // 사용자 예약 목록
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<BookingResponseDto>> getBookingsByUser(@PathVariable Long userId) {
        return ResponseEntity.ok(flBookService.getBookingsByUser(userId));
    }

    // 예약 취소
    @PostMapping("/{id}/cancel")
    public ResponseEntity<Void> cancelBooking(@PathVariable Long id) {
        flBookService.cancelBooking(id);

        return ResponseEntity.noContent().build();
    }
}
