package bitc.full502.spring.controller;

import bitc.full502.spring.domain.entity.Flight;
import bitc.full502.spring.service.FlightService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/flights")
@RequiredArgsConstructor
public class FlightReservationController {

    private final FlightService flightService;

    @GetMapping("/search")
    public ResponseEntity<List<Flight>> searchFlights(@RequestParam String dep, @RequestParam String arr) {

        List<Flight> res = flightService.searchFlights(dep, arr);

        return ResponseEntity.ok(res);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Flight> getFlight(@PathVariable Long id) {
        Flight flight = flightService.getFlight(id);
        return ResponseEntity.ok(flight);
    }
}
