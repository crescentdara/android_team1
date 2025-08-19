package bitc.full502.spring.domain.repository;

import bitc.full502.spring.domain.entity.FlBook;
import bitc.full502.spring.domain.entity.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface FlBookRepository extends JpaRepository<FlBook, Long> {

    List<FlBook> findByUser(Users user);

    // 특정 비행기/여행일의 이미 예약된 좌석 수 (adult+child, CANCEL 제외)
    @Query("""
        SELECT COALESCE(SUM(COALESCE(b.adult,0) + COALESCE(b.child,0)), 0)
        FROM FlBook b
        WHERE b.flight.id = :flightId
          AND b.tripDate   = :tripDate
          AND (b.status IS NULL OR b.status <> 'CANCEL')
    """)
    long countBookedSeats(@Param("flightId") Long flightId,
                          @Param("tripDate") LocalDate tripDate);
}
