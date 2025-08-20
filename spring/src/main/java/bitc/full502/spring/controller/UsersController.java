package bitc.full502.spring.controller;

import bitc.full502.spring.domain.entity.Users;
import bitc.full502.spring.domain.repository.UsersRepository;
import bitc.full502.spring.dto.*;
import bitc.full502.spring.service.UserCleanupService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


import java.net.URI;
import java.util.Map;
import java.util.Optional;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class UsersController {

    private final UsersRepository usersRepository;
    private final UserCleanupService userCleanupService;

    // ------------------------- 로그인 -------------------------
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequestDto request) {
        Optional<Users> userOpt = usersRepository.findByUsersId(request.getUsersId());
        if (userOpt.isPresent() && userOpt.get().getPass().equals(request.getPass())) {
            Users u = userOpt.get();
            // ✅ id 포함
            return ResponseEntity.ok(
                    new LoginResponseDto(u.getId(), u.getUsersId(), u.getName(), u.getEmail(), u.getPhone())
            );
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("아이디 또는 비밀번호가 잘못되었습니다.");
    }

    // ------------------------- 아이디/비번 찾기 -------------------------
    @PostMapping("/find-id")
    public ResponseEntity<?> findUserId(@RequestBody FindIdRequestDto request) {
        return usersRepository.findByEmailAndPass(request.getEmail(), request.getPass())
                .<ResponseEntity<?>>map(u -> ResponseEntity.ok(Map.of("usersId", u.getUsersId())))
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).body("일치하는 사용자 정보를 찾을 수 없습니다."));
    }

    @PostMapping("/find-password")
    public ResponseEntity<?> findUserPassword(@RequestBody FindPasswordRequestDto request) {
        return usersRepository.findByUsersIdAndEmail(request.getUsersId(), request.getEmail())
                .<ResponseEntity<?>>map(u -> ResponseEntity.ok(Map.of("pass", u.getPass())))
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).body("일치하는 사용자 정보를 찾을 수 없습니다."));
    }

    // ------------------------- 회원가입 (V1/V2) -------------------------
    @PostMapping("/signup")
    public ResponseEntity<?> signup(@RequestBody SignupRequestDto req) {
        if (usersRepository.existsByUsersId(req.getUsersId())) {
            return ResponseEntity.status(409).body("usersId already exists");
        }
        if (usersRepository.existsByEmail(req.getEmail())) {
            return ResponseEntity.status(409).body("email already exists");
        }
        Users u = Users.builder()
                .usersId(req.getUsersId())
                .email(req.getEmail())
                .pass(req.getPass())
                .name(req.getName())
                .phone(req.getPhone())
                .build();
        usersRepository.save(u);
        return ResponseEntity.created(URI.create("/api/users/" + u.getUsersId())).build();
    }

    // ------- 호환(V1) 도우미 (있던 코드 유지해도 무방) -------
    @GetMapping("/checkId")
    public ResponseEntity<?> checkIdV1(@RequestParam("id") String id) {
        boolean available = !usersRepository.existsByUsersId(id);
        return ResponseEntity.ok(Map.of("available", available));
    }

    @GetMapping("/user-info")
    public ResponseEntity<?> getUserV1(@RequestParam("userId") String userId) {
        return getUserV2(userId);
    }

    @PutMapping("/update-user")
    public ResponseEntity<?> updateUserV1(@RequestBody SignupRequestDto req) {
        return usersRepository.findByUsersId(req.getUsersId())
                .map(u -> {
                    u.setName(req.getName());
                    u.setEmail(req.getEmail());
                    u.setPhone(req.getPhone());
                    usersRepository.save(u);
                    return ResponseEntity.ok(Map.of("message", "ok"));
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /** 사용자 삭제 V2 */
    @DeleteMapping("/users/{usersId}/v2")
    public void deleteUserV2(@PathVariable String usersId) {
        userCleanupService.cascadeDeleteByUsersId(usersId);
    }

    // ------------------------- V2 표준 엔드포인트 -------------------------

    /** 회원가입 V2 */
    @PostMapping("/users/register")
    public ResponseEntity<?> registerV2(@RequestBody SignupRequestDto req) {
        if (usersRepository.existsByUsersId(req.getUsersId())) {
            return ResponseEntity.status(409).body("usersId already exists");
        }
        if (usersRepository.existsByEmail(req.getEmail())) {
            return ResponseEntity.status(409).body("email already exists");
        }
        Users u = Users.builder()
                .usersId(req.getUsersId())
                .email(req.getEmail())
                .pass(req.getPass())
                .name(req.getName())
                .phone(req.getPhone())
                .build();
        usersRepository.save(u);
        return ResponseEntity.created(URI.create("/api/users/" + u.getUsersId())).build();
    }

    /** 아이디 중복체크 V2 */
    @GetMapping("/users/check-id")
    public ResponseEntity<?> checkIdV2(@RequestParam("usersId") String usersId) {
        boolean available = !usersRepository.existsByUsersId(usersId);
        return ResponseEntity.ok(Map.of("available", available));
    }

    /** ✅ 사용자 조회 V2: /api/users/{usersId}  ← MyPageActivity가 호출 */
    @GetMapping("/users/{usersId}")
    public ResponseEntity<?> getUserV2(@PathVariable String usersId) {
        return usersRepository.findByUsersId(usersId)
                .<ResponseEntity<?>>map(u -> ResponseEntity.ok(
                        new UserResponseDto(u.getUsersId(), u.getName(), u.getEmail(), u.getPhone())
                ))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /** 사용자 업데이트 V2 (pass 제외) */
    @PutMapping("/users")
    public ResponseEntity<?> updateUserV2(@RequestBody UpdateUserRequestDto req) {
        return usersRepository.findByUsersId(req.getUsersId())
                .map(u -> {
                    u.setName(req.getName());
                    u.setEmail(req.getEmail());
                    u.setPhone(req.getPhone());
                    usersRepository.save(u);
                    return ResponseEntity.ok(
                            new UserResponseDto(u.getUsersId(), u.getName(), u.getEmail(), u.getPhone())
                    );
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

}
