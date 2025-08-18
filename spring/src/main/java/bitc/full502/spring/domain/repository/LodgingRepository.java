package bitc.full502.spring.domain.repository;

import bitc.full502.spring.domain.entity.Lodging;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

@Repository
public interface LodgingRepository extends JpaRepository<Lodging, Long> {
    @Query("select distinct l.city from Lodging l where l.city is not null order by l.city")
    List<String> findDistinctCities();

    @Query("select distinct l.town from Lodging l where l.city = :city and l.town is not null order by l.town")
    List<String> findDistinctTownsByCity(@Param("city") String city);

    @Query("select distinct l.vill from Lodging l where l.city = :city and l.town = :town and l.vill is not null order by l.vill")
    List<String> findDistinctVills(@Param("city") String city, @Param("town") String town);
}