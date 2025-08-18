package bitc.full502.spring.domain.repository;

import bitc.full502.spring.domain.entity.Users;
import org.springframework.data.jpa.repository.JpaRepository;

<<<<<<< HEAD
<<<<<<< HEAD
import java.util.Optional;

public interface UsersRepository extends JpaRepository<Users, Long> {

    Users findByUsersId(String usersId);              // 기존 메서드 유지
    boolean existsByUsersId(String usersId);          // 아이디 중복 체크용

    // 🔽 아이디 찾기 (이름과 이메일로)
    Optional<Users> findByEmailAndPass(String email, String pass);

    // 🔽 비밀번호 찾기 (전화번호와 이메일로)
    Optional<Users> findByUsersIdAndEmail(String usersId, String email);
=======
public interface UsersRepository extends JpaRepository<Users, Long> {
>>>>>>> testmerge/LodgingMerge
=======
import java.util.Optional;

public interface UsersRepository extends JpaRepository<Users, Long> {
    Optional<Users> findByUsersId(String usersId);
>>>>>>> testmerge/PostChatMerge
}
