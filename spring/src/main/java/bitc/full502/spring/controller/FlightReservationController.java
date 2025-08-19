package bitc.full502.spring.controller;

import bitc.full502.spring.domain.entity.Flight;
import bitc.full502.spring.domain.repository.FlBookRepository;
import bitc.full502.spring.service.FlightService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/flight")
@RequiredArgsConstructor
@Slf4j
public class FlightReservationController {

    private final FlightService flightService;
    private final FlBookRepository flBookRepository;

    @GetMapping("/search")
    public ResponseEntity<List<Flight>> searchFlights(
            @RequestParam String dep,
            @RequestParam String arr,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) String date,
            @RequestParam(required = false) String depTime
    ) {
        String depKey = normalizeAirport(dep);
        String arrKey = normalizeAirport(arr);
        LocalTime time = parseDepTimeOrNull(depTime);
        String day = toKoreanDow(parseDateStrict(date));

        List<Flight> list = flightService.searchFlightsByDay(depKey, arrKey, day, time);

        // ★ 좌석이 가득 찬 항공편(=20석 이상 예약됨)은 제외
        LocalDate tripDate = parseDateStrict(date);
        List<Flight> filtered = list.stream()
                .filter(f -> {
                    int total = (f.getTotalSeat() == null ? 20 : f.getTotalSeat());
                    long booked = flBookRepository.countBookedSeats(f.getId(), tripDate);
                    return booked < total;
                })
                .collect(Collectors.toList());

        log.info("FLIGHT_SEARCH dep={} arr={} day={} depTime={} -> {} rows (filtered from {})",
                depKey, arrKey, day, (time == null ? "null" : time),
                filtered.size(), list.size());

        return ResponseEntity.ok(filtered);
    }

    /* ---------- helpers ---------- */

    private static String normalizeAirport(String s) {
        if (s == null) return "";
        // "김포(서울)" -> "김포(서울)" 그대로 두되 공백 트림
        return s.trim();
    }

    private static LocalTime parseDepTimeOrNull(String hhmm) {
        if (hhmm == null || hhmm.isBlank()) return null;
        try {
            // "08:00" 또는 "800" 모두 수용
            String v = hhmm.trim();
            if (v.matches("^\\d{3,4}$")) {
                v = (v.length() == 3 ? "0" + v : v);
                v = v.substring(0, 2) + ":" + v.substring(2);
            }
            return LocalTime.parse(v);
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    private static LocalDate parseDateStrict(String isoYmd) {
        return LocalDate.parse(isoYmd, DateTimeFormatter.ISO_LOCAL_DATE);
    }

    private static String toKoreanDow(LocalDate date) {
        return switch (date.getDayOfWeek()) {
            case MONDAY    -> "월";
            case TUESDAY   -> "화";
            case WEDNESDAY -> "수";
            case THURSDAY  -> "목";
            case FRIDAY    -> "금";
            case SATURDAY  -> "토";
            case SUNDAY    -> "일";
        };
    }
}
