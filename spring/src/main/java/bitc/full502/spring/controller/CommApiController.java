package bitc.full502.spring.controller;

import bitc.full502.spring.dto.CommDto;
import bitc.full502.spring.service.CommService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/comments")
public class CommApiController {

    private final CommService commService;

    private String requireUser(String usersId) {
        if (usersId == null || usersId.isBlank())
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "X-USER-ID header required");
        return usersId;
    }

    // 목록
    @GetMapping("/{postId}")
    public List<CommDto> list(@PathVariable Long postId) {
        return commService.list(postId);
    }

    // 작성 (form-url-encoded)
    @PostMapping(consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public Long write(@RequestParam Long postId,
                      @RequestParam(required = false) Long parentId,
                      @RequestParam String content,
                      @RequestHeader(value = "X-USER-ID", required = false) String usersId) {
        return commService.write(postId, parentId, content, requireUser(usersId));
    }

    // 수정 (form-url-encoded)
    @PutMapping(value = "/{id}", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public ResponseEntity<Void> edit(@PathVariable Long id,
                                     @RequestParam String content,
                                     @RequestHeader(value = "X-USER-ID", required = false) String usersId) {
        commService.edit(id, content, requireUser(usersId));
        return ResponseEntity.ok().build();
    }

    // 삭제
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id,
                                       @RequestHeader(value = "X-USER-ID", required = false) String usersId) {
        commService.remove(id, requireUser(usersId));
        return ResponseEntity.ok().build();
    }
}
