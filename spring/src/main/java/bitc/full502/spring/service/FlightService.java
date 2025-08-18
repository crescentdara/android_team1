package bitc.full502.spring.service;

import bitc.full502.spring.domain.entity.Flight;
import jakarta.annotation.Nullable;

import java.time.LocalTime;
import java.util.List;

// Service
public interface FlightService {
    List<Flight> searchFlightsByDay(String dep, String arr, String day, @Nullable LocalTime depTime);
    Flight getFlight(Long id);
}

