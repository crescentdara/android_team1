package bitc.full502.spring.service;
import bitc.full502.spring.domain.entity.Flight;
import bitc.full502.spring.domain.repository.FlightRepository;
import bitc.full502.spring.service.FlightService;
import jakarta.annotation.Nullable;
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
public class FlightServiceImpl implements bitc.full502.spring.service.FlightService {

    private final FlightRepository flightRepository;

    @Override
    public List<Flight> searchFlightsByDay(String dep, String arr, String day, @Nullable LocalTime depTime) {
        // FlightRepository 에 JPQL or Query Method 필요
        if (depTime == null) {
            return flightRepository.findByDepAndArrAndDaysContaining(dep, arr, day);
        } else {
            return flightRepository.findByDepAndArrAndDaysContainingAndDepTimeAfter(dep, arr, day, depTime);
        }
    }

    @Override
    public Flight getFlight(Long id) {
        return flightRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 ID의 항공편이 존재하지 않습니다. id=" + id));
    }
}
