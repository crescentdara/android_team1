package bitc.full502.spring.domain.repository;

import bitc.full502.spring.domain.entity.LodCnt;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface LodCntRepository extends JpaRepository<LodCnt, Long> {

    @Modifying
    @Transactional
    @Query(value =
            "INSERT INTO lod_cnt (lod_id, views, wish_cnt, book_cnt) " +
                    "VALUES (:lodgingId, 0, 0, 0) " +
                    "ON DUPLICATE KEY UPDATE lod_id = lod_id", nativeQuery = true)
    void ensureCounterRow(@Param("lodgingId") Long lodgingId);

    @Modifying
    @Transactional
    @Query(value =
            "UPDATE lod_cnt " +
                    "SET views = IFNULL(views, 0) + 1 " +
                    "WHERE lod_id = :lodgingId", nativeQuery = true)
    int incrementViews(@Param("lodgingId") Long lodgingId);

    @Query(value = "SELECT IFNULL(views, 0) FROM lod_cnt WHERE lod_id = :lodgingId", nativeQuery = true)
    Long getViews(@Param("lodgingId") Long lodgingId);

    @Query(value = "SELECT COUNT(*) FROM lod_wish WHERE lod_id = :lodgingId", nativeQuery = true)
    Long countWish(@Param("lodgingId") Long lodgingId);

    @Query(value =
            "SELECT COUNT(*) " +
                    "FROM lod_book " +
                    "WHERE lod_id = :lodgingId " +
                    "AND (status IS NULL OR status <> 'CANCEL')", nativeQuery = true)
    Long countBooking(@Param("lodgingId") Long lodgingId);
}

