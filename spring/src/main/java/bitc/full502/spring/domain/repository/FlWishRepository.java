package bitc.full502.spring.domain.repository;

import bitc.full502.spring.domain.entity.FlWish;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FlWishRepository extends JpaRepository<FlWish, Long> {
    boolean existsByUser_IdAndFlight_Id(Long userId, Long flightId);
    Optional<FlWish> findByUser_IdAndFlight_Id(Long userId, Long flightId);
    long countByFlight_Id(Long flightId);
    void deleteByUser_IdAndFlight_Id(Long userId, Long flightId);

    List<FlWish> findByUser_Id(Long userId);
}
