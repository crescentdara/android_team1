package bitc.full502.spring.domain.repository;

import bitc.full502.spring.domain.entity.LodBook;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;

public interface LodBookRepository extends JpaRepository<LodBook, Long> {
    /**
     * 기간이 겹치는 예약 수를 센다.
     * (겹침 조건) 기존.ckIn < 요청.checkOut  AND  기존.ckOut > 요청.checkIn
     * 취소(CANCEL)는 제외한다.
     *
     * JPQL을 사용하여 엔티티 필드명을 그대로 사용 → DB 컬럼명 불일치 문제 방지
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
}