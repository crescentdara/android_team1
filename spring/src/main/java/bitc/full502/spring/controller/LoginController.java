package bitc.full502.spring.controller;

import bitc.full502.spring.domain.entity.Users;
import bitc.full502.spring.domain.repository.UsersRepository;
import bitc.full502.spring.dto.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api")
public class LoginController {

    private final UsersRepository usersRepository;

    public LoginController(UsersRepository usersRepository) {
        this.usersRepository = usersRepository;
    }

    // ✅ 로그인
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequestDto request) {
        Optional<Users> userOpt = usersRepository.findByUsersId(request.getUserId());

        if (userOpt.isPresent() && userOpt.get().getPass().equals(request.getPassword())) {
            Users user = userOpt.get();
            LoginResponseDto response = new LoginResponseDto(
                    user.getUsersId(),
                    user.getName(),
                    user.getEmail(),
                    user.getPhone()
            );
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("아이디 또는 비밀번호가 잘못되었습니다.");
        }
    }

    // ✅ 회원가입
    @PostMapping("/signup")
    public ResponseEntity<?> signup(@RequestBody Users user) {
        if (usersRepository.existsByUsersId(user.getUsersId())) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("이미 존재하는 아이디입니다.");
        }
        usersRepository.save(user);
        return ResponseEntity.ok("회원가입 완료");
    }

    // ✅ 비밀번호 찾기 (아이디 + 이메일)
    @PostMapping("/find-password")
    public ResponseEntity<?> findUserPassword(@RequestBody FindPasswordRequestDto request) {
        return usersRepository.findByUsersIdAndEmail(request.getUsersId(), request.getEmail())
                .<ResponseEntity<?>>map(u -> ResponseEntity.ok(Map.of("password", u.getPass())))
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("일치하는 사용자 정보를 찾을 수 없습니다."));
    }

    // ✅ 아이디 찾기 (이메일 + 비밀번호)
    @PostMapping("/find-id")
    public ResponseEntity<?> findUserId(@RequestBody FindIdRequestDto request) {
        return usersRepository.findByEmailAndPass(request.getEmail(), request.getPass())
                .<ResponseEntity<?>>map(u -> ResponseEntity.ok(Map.of("userId", u.getUsersId())))
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("일치하는 사용자 정보를 찾을 수 없습니다."));
    }

}
