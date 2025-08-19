package bitc.full502.spring.service;
import bitc.full502.spring.domain.entity.Flight;
import bitc.full502.spring.domain.repository.FlightRepository;
import bitc.full502.spring.service.FlightService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalTime;
import java.util.List;

// ServiceImpl
@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class FlightServiceImpl implements FlightService {
    private final FlightRepository flightRepository;

    @Override
    public List<Flight> searchFlightsByDay(String dep, String arr, String day, LocalTime depTime) {
        List<Flight> results = flightRepository.searchByDepArrDay(dep, arr, day, depTime);
        log.info("DB_RESULTS dep={} arr={} day={} depTime={} -> {} rows",
                dep, arr, day, depTime, results.size());
        return results;
    }

    @Override
    public Flight getFlight(Long id) {
        return flightRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Flight not found: " + id));
    }
}
