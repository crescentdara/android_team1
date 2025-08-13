package bitc.full502.spring.domain.repository;

import bitc.full502.spring.domain.entity.Flight;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FlightRepository extends JpaRepository<Flight, Long> {
    List<Flight> findByDepAndArr(String dep, String arr);
}
