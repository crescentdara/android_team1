package bitc.full502.spring.domain.repository;

import bitc.full502.spring.domain.entity.Users;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UsersRepository extends JpaRepository<Users, Long> {
}
