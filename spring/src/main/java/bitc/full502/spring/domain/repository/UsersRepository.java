package bitc.full502.spring.domain.repository;

import bitc.full502.spring.domain.entity.Users;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UsersRepository extends JpaRepository<Users, Long> {

    Optional<Users> findByUsersId(String usersId);

    boolean existsByUsersId(String usersId);

    Optional<Users> findByEmailAndPass(String email, String pass);

    Optional<Users> findByUsersIdAndEmail(String usersId, String email);

    boolean existsByEmail(String email);
}
