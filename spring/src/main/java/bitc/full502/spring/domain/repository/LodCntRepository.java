package bitc.full502.spring.domain.repository;

import bitc.full502.spring.domain.entity.LodCnt;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface LodCntRepository extends JpaRepository<LodCnt, Long> {

    // lod_cnt에 해당 숙소 카운터가 없으면 0으로 생성 (있는 경우 아무 일도 안 함)
    @Modifying
    @Transactional
    @Query(value = """
            INSERT INTO lod_cnt (lod_id, views, wish_cnt, book_cnt)
            VALUES (:lodgingId, 0, 0, 0)
            ON DUPLICATE KEY UPDATE lod_id = lod_id
            """, nativeQuery = true)
    void ensureCounterRow(@Param("lodgingId") Long lodgingId);

    // 조회수 1 증가
    @Modifying
    @Transactional
    @Query(value = """
            UPDATE lod_cnt
            SET views = IFNULL(views, 0) + 1
            WHERE lod_id = :lodgingId
            """, nativeQuery = true)
    int incrementViews(@Param("lodgingId") Long lodgingId);

    // 현재 조회수 조회
    @Query(value = "SELECT IFNULL(views, 0) FROM lod_cnt WHERE lod_id = :lodgingId", nativeQuery = true)
    Long getViews(@Param("lodgingId") Long lodgingId);

    // 찜 수
    @Query(value = "SELECT COUNT(*) FROM lod_wish WHERE lod_id = :lodgingId", nativeQuery = true)
    Long countWish(@Param("lodgingId") Long lodgingId);

    // 예약 수: CANCEL 제외
    @Query(value = """
            SELECT COUNT(*)
            FROM lod_book
            WHERE lod_id = :lodgingId
              AND (status IS NULL OR status <> 'CANCEL')
            """, nativeQuery = true)
    Long countBooking(@Param("lodgingId") Long lodgingId);
}
