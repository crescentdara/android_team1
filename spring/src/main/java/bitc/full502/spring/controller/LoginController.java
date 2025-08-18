package bitc.full502.spring.controller;

import bitc.full502.spring.domain.repository.UsersRepository;
import bitc.full502.spring.domain.entity.Users;
import bitc.full502.spring.dto.LoginRequestDto;
import bitc.full502.spring.dto.LoginResponseDto;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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

    // 회원가입 API 추가
    @PostMapping("/signup")
    public ResponseEntity<?> signup(@RequestBody Users user) {
        // 아이디 중복 확인
        if (usersRepository.findByUsersId(user.getUsersId()) != null) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("이미 존재하는 아이디입니다.");
        }
        // 패스워드 등 추가 유효성 검사는 필요 시 추가하세요.

        usersRepository.save(user);
        return ResponseEntity.ok("회원가입 완료");
    }
}

