package bitc.full502.spring.domain.repository;

import bitc.full502.spring.domain.entity.Lodging;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LodgingRepository extends JpaRepository<Lodging, Long> {
}

