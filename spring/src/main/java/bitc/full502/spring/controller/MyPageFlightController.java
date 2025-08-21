package bitc.full502.spring.controller;

import bitc.full502.spring.dto.BookingResponseDto;
import bitc.full502.spring.dto.FlightWishDto;
import bitc.full502.spring.service.FlBookService;
import bitc.full502.spring.domain.repository.FlWishRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/mypage/flight")
@CrossOrigin(origins = "*")
public class MyPageFlightController {

    private final FlBookService flBookService;

    /** ✅ 1) 항공 예매내역 */
    @GetMapping("/bookings")
    public List<BookingResponseDto> getUserBookings(@RequestParam("userPk") Long userPk) {
        return flBookService.getBookingsByUser(userPk);
    }

}
