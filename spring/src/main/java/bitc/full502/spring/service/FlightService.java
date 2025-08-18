package bitc.full502.spring.service;

import bitc.full502.spring.domain.entity.Flight;

import java.time.LocalTime;
import java.util.List;

public interface FlightService {
    List<Flight> searchFlights(String dep, String arr, LocalTime depTime);
    Flight getFlight(Long id);

}
