package bitc.full502.spring.controller;

import bitc.full502.spring.domain.entity.Flight;
import bitc.full502.spring.service.FlightService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/flights")
@RequiredArgsConstructor
@CrossOrigin(origins = "*") // 개발 중 CORS 편의
public class FlightReservationController {

    private final FlightService flightService;

    private static final DateTimeFormatter HHMM = DateTimeFormatter.ofPattern("HH:mm");
    private static final String[] K_DOW = {"월","화","수","목","금","토","일"};

    // IATA 코드 → DB에 저장된 한글 공항명 매핑 (필요시 추가)
    private static final Map<String, String> CODE2NAME = Map.of(
            "GMP","서울/김포",
            "ICN","서울/인천",
            "CJU","제주",
            "CJJ","청주",
            "MWX","무안",
            "PUS","부산/김해"
    );

    private static String toKoreanDow(LocalDate date) {
        return K_DOW[date.getDayOfWeek().getValue() - 1]; // MON=1..SUN=7 → 0..6
    }

    private static String normalizeAirport(String raw) {
        if (raw == null) return null;
        String trimmed = raw.trim();
        String upper = trimmed.toUpperCase();
        // 코드면 매핑, 한글이면 그대로
        return CODE2NAME.getOrDefault(upper, trimmed);
    }

    private static LocalTime parseDepTimeOrNull(String depTime) {
        if (depTime == null || depTime.isBlank()) return null;
        try {
            return LocalTime.parse(depTime.trim(), HHMM);
        } catch (DateTimeParseException e) {
            // 잘못된 포맷이면 필터 무시
            return null;
        }
    }

    /**
     * 예: /api/flights/search?dep=GMP&arr=CJU&date=2025-09-03&depTime=09:00
     *  - dep/arr: IATA 코드 또는 한글 공항명 모두 허용
     *  - date: yyyy-MM-dd (하루)
     *  - depTime: HH:mm (선택)
     */
    @GetMapping("/search")
    public ResponseEntity<List<Flight>> searchFlights(
            @RequestParam String dep,
            @RequestParam String arr,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false) String depTime
    ) {
        // 원본 로그
        log.info("REQ /api/flights/search raw dep='{}' arr='{}' date={} depTime='{}'",
                dep, arr, date, depTime);

        String depKey = normalizeAirport(dep);
        String arrKey = normalizeAirport(arr);
        LocalTime time = parseDepTimeOrNull(depTime);
        String day = toKoreanDow(date);

        List<Flight> list = flightService.searchFlightsByDay(depKey, arrKey, day, time);

        log.info("FLIGHT_SEARCH dep={} arr={} day={} depTime={} -> {} rows",
                depKey, arrKey, day, (time == null ? "null" : time), list.size());

        return ResponseEntity.ok(list);
    }
}
