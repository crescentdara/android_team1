package bitc.full502.spring.domain.repository;

import bitc.full502.spring.domain.entity.LodBook;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;

public interface LodBookRepository extends JpaRepository<LodBook, Long> {

    /**
     * 기간이 겹치는 예약 수 (CANCEL 제외)
     */
    @Query("""
            SELECT COUNT(b)
            FROM LodBook b
            WHERE b.lodging.id = :lodgingId
              AND b.ckIn < :checkOut
              AND b.ckOut > :checkIn
              AND (b.status IS NULL OR b.status <> 'CANCEL')
           """)
    long countOverlapping(
            @Param("lodgingId") Long lodgingId,
            @Param("checkIn") LocalDate checkIn,
            @Param("checkOut") LocalDate checkOut
    );

    /**
     * 누적 예약 수 (CANCEL 제외)
     */
    @Query("""
            SELECT COUNT(b)
            FROM LodBook b
            WHERE b.lodging.id = :lodgingId
              AND (b.status IS NULL OR b.status <> 'CANCEL')
           """)
    long countActive(@Param("lodgingId") Long lodgingId);
}
