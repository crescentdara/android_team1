package bitc.full502.spring.domain.repository;

import bitc.full502.spring.domain.entity.LodBook;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;

public interface LodBookRepository extends JpaRepository<LodBook, Long> {
    /**
     * 기간이 겹치는 예약 수를 센다.
     * (겹침 조건) 기존.check_in < 요청.checkOut  AND  기존.check_out > 요청.checkIn
     * 취소(CANCEL)는 제외한다.
     */
    @Query(value = """
        SELECT COUNT(*) 
        FROM lod_book b
        WHERE b.lodging_id = :lodgingId
          AND b.check_in < :checkOut
          AND b.check_out > :checkIn
          AND (b.status IS NULL OR b.status <> 'CANCEL')
    """, nativeQuery = true)
    long countOverlapping(
            @Param("lodgingId") Long lodgingId,
            @Param("checkIn") LocalDate checkIn,
            @Param("checkOut") LocalDate checkOut
    );
}