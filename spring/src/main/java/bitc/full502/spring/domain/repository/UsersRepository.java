package bitc.full502.spring.domain.repository;

import bitc.full502.spring.domain.entity.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UsersRepository extends JpaRepository<Users, Long> {

    Optional<Users> findByUsersId(String usersId);             // 기존 메서드 유지

    boolean existsByUsersId(String usersId);          // 아이디 중복 체크용

    Optional<Users> findByEmailAndPass(String email, String pass);

    Optional<Users> findByUsersIdAndEmail(String usersId, String email);
}



