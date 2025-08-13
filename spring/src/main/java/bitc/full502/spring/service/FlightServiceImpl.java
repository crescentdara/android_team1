package bitc.full502.spring.service;

import bitc.full502.spring.domain.entity.Flight;
import bitc.full502.spring.domain.repository.FlightRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FlightServiceImpl implements FlightService {
    private final FlightRepository flightRepository;

    @Override
    public List<Flight> searchFlights(String dep, String arr) {
        return flightRepository.findByDepAndArr(dep, arr);
    }

    @Override
    public Flight getFlight(Long id) {
        return flightRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Flight not found id=" + id));
    }
}
