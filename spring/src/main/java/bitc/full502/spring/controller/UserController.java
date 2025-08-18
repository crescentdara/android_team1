package bitc.full502.spring.controller;

import bitc.full502.spring.domain.entity.Users;
import bitc.full502.spring.domain.repository.UsersRepository;
import bitc.full502.spring.dto.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

@RestController
@RequestMapping("/api")
public class UserController {

    @Autowired
    private UsersRepository usersRepository;

    /** 📝 사용자 정보 수정 */
    @PutMapping("/update-user")
    public ResponseEntity<Map<String, String>> updateUser(@RequestBody SignupRequestDto dto) {
        String userId = dto.getUsersId().trim();
        System.out.println("[update-user] 요청: userId=" + userId);

        return usersRepository.findByUsersId(userId)
                .map(user -> {
                    user.setName(dto.getName().trim());
                    user.setEmail(dto.getEmail().trim());
                    user.setPass(dto.getPass().trim());
                    user.setPhone(dto.getPhone().trim());
                    usersRepository.save(user);
                    return ResponseEntity.ok(Map.of("message", "사용자 정보가 성공적으로 수정되었습니다."));
                })
                .orElseThrow(() ->
                        new ResponseStatusException(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다.")
                );
    }

    /** ❌ 회원 탈퇴 */
    @DeleteMapping("/delete-user")
    public ResponseEntity<Void> deleteUser(@RequestParam String userId) {
        userId = userId.trim();
        System.out.println("[delete-user] 요청: userId=" + userId);

        var opt = usersRepository.findByUsersId(userId);
        if (opt.isPresent()) {
            usersRepository.delete(opt.get());
            System.out.println("[delete-user] 삭제 완료");
            return ResponseEntity.noContent().build();
        } else {
            System.out.println("[delete-user] 사용자 없음");
            return ResponseEntity.notFound().build();
        }
    }


    /** 🔍 사용자 정보 조회 */
    @GetMapping("/user-info")
    public ResponseEntity<SignupRequestDto> getUserInfo(@RequestParam String userId) {
        userId = userId.trim();
        System.out.println("[user-info] 요청: userId=" + userId);

        return usersRepository.findByUsersId(userId)
                .map(user -> {
                    SignupRequestDto res = new SignupRequestDto(
                            user.getName(),
                            user.getUsersId(),
                            user.getPass(),
                            user.getEmail(),
                            user.getPhone()
                    );
                    return ResponseEntity.ok(res);
                })
                .orElseThrow(() -> {
                    System.out.println("[user-info] 유저 정보 없음");
                    return new ResponseStatusException(HttpStatus.NOT_FOUND, "유저 정보를 찾을 수 없습니다.");
                });
    }

    /** ✅ ID 중복 체크 */
    @GetMapping("/checkId")
    public ResponseEntity<CheckIdResponse> checkId(@RequestParam String id) {
        boolean exists = usersRepository.existsByUsersId(id.trim());
        return ResponseEntity.ok(new CheckIdResponse(!exists)); // true: 사용 가능
    }

    // 내부 응답 DTO
    static class CheckIdResponse {
        private boolean available;
        public CheckIdResponse(boolean available) { this.available = available; }
        public boolean isAvailable() { return available; }
        public void setAvailable(boolean available) { this.available = available; }
    }
}
