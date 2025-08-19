package bitc.full502.spring.domain.repository;

import bitc.full502.spring.domain.entity.FlBook;
import bitc.full502.spring.domain.entity.Users;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FlBookRepository extends JpaRepository<FlBook, Long> {
    List<FlBook> findByUser(Users user);
}
