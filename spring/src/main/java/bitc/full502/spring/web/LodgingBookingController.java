package bitc.full502.spring.web;

import bitc.full502.spring.dto.LodgingBookingDto;
import bitc.full502.spring.service.LodgingBookingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/lodging")
@RequiredArgsConstructor
public class LodgingBookingController {

    private final LodgingBookingService lodgingBookingService;

    @PostMapping("/book")
    public ResponseEntity<?> bookLodging(@RequestBody LodgingBookingDto dto) {
        lodgingBookingService.saveBooking(dto);
        return ResponseEntity.ok().body(Map.of("message", "예약이 완료되었습니다."));
    }
}

