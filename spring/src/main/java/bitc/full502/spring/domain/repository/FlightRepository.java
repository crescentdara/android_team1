package bitc.full502.spring.domain.repository;

import bitc.full502.spring.domain.entity.Flight;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalTime;
import java.util.List;

@Repository
public interface FlightRepository extends JpaRepository<Flight, Long> {
    List<Flight> findByDepAndArr(String dep, String arr);

    // 출발지, 도착지, 출발시간 이후 검색
    @Query("SELECT f FROM Flight f " +
            "WHERE f.dep = :dep " +
            "AND f.arr = :arr " +
            "AND f.depTime >= :depTime")
    List<Flight> searchFlights(@Param("dep") String dep,
                               @Param("arr") String arr,
                               @Param("depTime") LocalTime depTime);

}
