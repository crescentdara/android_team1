package bitc.full502.spring.service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
public class AvailabilityQueryService {
    @PersistenceContext
    private EntityManager em;

    /**
     * 기간이 겹치는 예약 수
     * 겹침: existing.ckIn < :checkOut AND existing.ckOut > :checkIn
     * CANCEL 제외
     */
    public long countOverlapping(Long lodgingId, LocalDate checkIn, LocalDate checkOut) {
        String jpql = """
            SELECT COUNT(b)
            FROM LodBook b
            WHERE b.lodging.id = :lodgingId
              AND b.ckIn < :checkOut
              AND b.ckOut > :checkIn
              AND (b.status IS NULL OR b.status <> 'CANCEL')
        """;
        return em.createQuery(jpql, Long.class)
                .setParameter("lodgingId", lodgingId)
                .setParameter("checkIn", checkIn)
                .setParameter("checkOut", checkOut)
                .getSingleResult();
    }
}
