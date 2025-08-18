package bitc.full502.spring.controller;

import bitc.full502.spring.domain.entity.Users;
import bitc.full502.spring.domain.repository.UsersRepository;
import bitc.full502.spring.dto.*;
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

    // ------------------------- 인증/계정 찾기 -------------------------

    /** 로그인 (공용) */
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequestDto request) {
        Optional<Users> userOpt = usersRepository.findByUsersId(request.getUsersId());
        if (userOpt.isPresent() && userOpt.get().getPass().equals(request.getPass())) {
            Users u = userOpt.get();
            return ResponseEntity.ok(new LoginResponseDto(u.getUsersId(), u.getName(), u.getEmail(), u.getPhone()));
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("아이디 또는 비밀번호가 잘못되었습니다.");
    }

    /** 아이디 찾기 (email + pass -> usersId) */
    @PostMapping("/find-id")
    public ResponseEntity<?> findUserId(@RequestBody FindIdRequestDto request) {
        return usersRepository.findByEmailAndPass(request.getEmail(), request.getPass())
                .<ResponseEntity<?>>map(u -> ResponseEntity.ok(Map.of("usersId", u.getUsersId())))
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).body("일치하는 사용자 정보를 찾을 수 없습니다."));
    }

    /** 비밀번호 찾기 (usersId + email -> pass) */
    @PostMapping("/find-password")
    public ResponseEntity<?> findUserPassword(@RequestBody FindPasswordRequestDto request) {
        return usersRepository.findByUsersIdAndEmail(request.getUsersId(), request.getEmail())
                .<ResponseEntity<?>>map(u -> ResponseEntity.ok(Map.of("pass", u.getPass())))
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).body("일치하는 사용자 정보를 찾을 수 없습니다."));
    }

    // ------------------------- 회원가입 -------------------------

    /** 회원가입 V1 (기존) */
    @PostMapping("/signup")
    public ResponseEntity<?> signupV1(@RequestBody SignupRequestDto req) {
        return registerInternal(req);
    }

    /** 회원가입 V2 (표준) */
    @PostMapping("/users/register")
    public ResponseEntity<?> registerV2(@RequestBody SignupRequestDto req) {
        return registerInternal(req);
    }

    private ResponseEntity<?> registerInternal(SignupRequestDto req) {
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
        // V1 클라이언트는 200도 OK, V2는 201을 선호 → 201 Created로 응답하고 Location 제공
        return ResponseEntity.created(URI.create("/api/users/" + u.getUsersId())).build();
    }

    // ------------------------- 조회/수정/삭제 -------------------------

    /** 사용자 조회 V2 */
    @GetMapping("/users/{usersId}")
    public ResponseEntity<?> getUserV2(@PathVariable String usersId) {
        return usersRepository.findByUsersId(usersId)
                .<ResponseEntity<?>>map(u -> ResponseEntity.ok(new UserResponseDto(
                        u.getUsersId(), u.getName(), u.getEmail(), u.getPhone()
                )))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /** 사용자 수정 V2 (pass 제외) */
    @PutMapping("/users")
    public ResponseEntity<?> updateUserV2(@RequestBody UpdateUserRequestDto req) {
        return usersRepository.findByUsersId(req.getUsersId())
                .map(u -> {
                    u.setName(req.getName());
                    u.setEmail(req.getEmail());
                    u.setPhone(req.getPhone());
                    usersRepository.save(u);
                    return ResponseEntity.ok(new UserResponseDto(
                            u.getUsersId(), u.getName(), u.getEmail(), u.getPhone()
                    ));
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /** 사용자 삭제 V2 */
    @DeleteMapping("/users/{usersId}")
    public ResponseEntity<?> deleteUserV2(@PathVariable String usersId) {
        return usersRepository.findByUsersId(usersId)
                .map(u -> {
                    usersRepository.delete(u);
                    return ResponseEntity.noContent().build();
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // ------------------------- 호환용(V1) 엔드포인트 -------------------------

    /** 아이디 중복체크 V1: /api/checkId?id=foo  → { "available": true|false } */
    @GetMapping("/checkId")
    public ResponseEntity<?> checkIdV1(@RequestParam("id") String id) {
        boolean available = !usersRepository.existsByUsersId(id);
        return ResponseEntity.ok(Map.of("available", available));
    }

    /** 아이디 중복체크 V2: /api/users/check-id?usersId=foo  → { "available": true|false } */
    @GetMapping("/users/check-id")
    public ResponseEntity<?> checkIdV2(@RequestParam("usersId") String usersId) {
        boolean available = !usersRepository.existsByUsersId(usersId);
        return ResponseEntity.ok(Map.of("available", available));
    }

    /** 사용자 조회 V1: /api/user-info?userId= */
    @GetMapping("/user-info")
    public ResponseEntity<?> getUserV1(@RequestParam("userId") String userId) {
        return getUserV2(userId);
    }

    /** 사용자 수정 V1: /api/update-user  (기존 앱이 SignupRequest 형태로 보냈던 것 호환)
     *  pass는 수정하지 않고 name/email/phone만 갱신, 응답: { "message": "ok" }
     */
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

    /** 사용자 삭제 V1: /api/delete-user?userId= */
    @DeleteMapping("/delete-user")
    public ResponseEntity<?> deleteUserV1(@RequestParam("userId") String userId) {
        return deleteUserV2(userId);
    }
}
