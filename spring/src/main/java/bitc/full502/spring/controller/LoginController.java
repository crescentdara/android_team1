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

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequestDto request) {
        Users user = usersRepository.findByUsersId(request.getUserId());

        if (user != null && user.getPass().equals(request.getPassword())) {
            LoginResponseDto response = new LoginResponseDto(
                    user.getUsersId(),
                    user.getName(),
                    user.getEmail(),
                    user.getPhone()
            );
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("아이디 또는 비밀번호가 잘못되었습니다.");
        }
    }

    @PostMapping("/signup")
    public ResponseEntity<?> signup(@RequestBody Users user) {
        if (usersRepository.findByUsersId(user.getUsersId()) != null) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("이미 존재하는 아이디입니다.");
        }
        usersRepository.save(user);
        return ResponseEntity.ok("회원가입 완료");
    }

    // ✅ 아이디 찾기 (이메일 + 비밀번호)
    @PostMapping("/find-id")
    public ResponseEntity<?> findUserId(@RequestBody FindIdRequestDto request) {
        Optional<Users> userOpt = usersRepository.findByEmailAndPass(request.getEmail(), request.getPass());

        if (userOpt.isPresent()) {
            Map<String, String> result = new HashMap<>();
            result.put("userId", userOpt.get().getUsersId());
            return ResponseEntity.ok(result);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("일치하는 사용자 정보를 찾을 수 없습니다.");
        }
    }

    // ✅ 비밀번호 찾기 (아이디 + 이메일)
    @PostMapping("/find-password")
    public ResponseEntity<?> findUserPassword(@RequestBody FindPasswordRequestDto request) {
        Optional<Users> userOpt = usersRepository.findByUsersIdAndEmail(request.getUsersId(), request.getEmail());

        if (userOpt.isPresent()) {
            Map<String, String> result = new HashMap<>();
            result.put("password", userOpt.get().getPass());
            return ResponseEntity.ok(result);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("일치하는 사용자 정보를 찾을 수 없습니다.");
        }
    }
}
