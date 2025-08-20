package bitc.full502.spring.domain.repository;

import bitc.full502.spring.domain.entity.LodWish;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LodWishRepository extends JpaRepository<LodWish, Long> {

    boolean existsByUser_IdAndLodging_Id(Long userId, Long lodId);

    Optional<LodWish> findByUser_IdAndLodging_Id(Long userId, Long lodId);

    long countByLodging_Id(Long lodId);

    void deleteByUser_IdAndLodging_Id(Long userId, Long lodId);

    // ✅ 유저별 즐겨찾기 목록 조회
    List<LodWish> findByUser_Id(Long userId);
}
