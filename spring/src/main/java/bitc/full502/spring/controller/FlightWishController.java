package bitc.full502.spring.web;

import bitc.full502.spring.domain.entity.FlWish;
import bitc.full502.spring.domain.entity.Flight;
import bitc.full502.spring.domain.entity.Users;
import bitc.full502.spring.domain.repository.FlWishRepository;
import bitc.full502.spring.domain.repository.FlightRepository;
import bitc.full502.spring.dto.FlightWishDto;
import bitc.full502.spring.dto.WishStatusDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class FlightWishController {

    private final FlWishRepository flWishRepository;
    private final FlightRepository flightRepository;

    // 상태 조회: /api/flight/{flightId}/wish/status  (헤더 X-USER-ID 필요)
    @GetMapping("/api/flight/{flightId}/wish/status")
    public WishStatusDto status(@PathVariable Long flightId,
                                @RequestHeader("X-USER-ID") Long userId) {
        boolean wished = flWishRepository.existsByUser_IdAndFlight_Id(userId, flightId);
        long count = flWishRepository.countByFlight_Id(flightId);
        return new WishStatusDto(wished, count);
    }

    // 토글: /api/flight/{flightId}/wish  (헤더 X-USER-ID 필요)
    @PutMapping("/api/flight/{flightId}/wish")
    @Transactional
    public WishStatusDto toggle(@PathVariable Long flightId,
                                @RequestHeader("X-USER-ID") Long userId) {
        Flight flight = flightRepository.findById(flightId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "flight not found"));

        flWishRepository.findByUser_IdAndFlight_Id(userId, flightId)
                .ifPresentOrElse(
                        flWishRepository::delete,
                        () -> {
                            Users userRef = Users.builder().id(userId).build();
                            FlWish wish = FlWish.builder().user(userRef).flight(flight).build();
                            flWishRepository.save(wish);
                        }
                );

        boolean nowWished = flWishRepository.existsByUser_IdAndFlight_Id(userId, flightId);
        long count = flWishRepository.countByFlight_Id(flightId);
        return new WishStatusDto(nowWished, count);
    }

    // 사용자 즐겨찾기 목록
    @GetMapping("/api/flight/wish/user/{userId}")
    public List<FlightWishDto> list(@PathVariable Long userId) {
        return flWishRepository.findByUser_Id(userId).stream()
                .map(w -> new FlightWishDto(
                        w.getId(),
                        w.getFlight().getAirline(),
                        w.getFlight().getFlNo(),
                        w.getFlight().getDep(),
                        w.getFlight().getArr(),
                        null // 썸네일 사용 안 하면 null 유지
                ))
                .toList();
    }
}
