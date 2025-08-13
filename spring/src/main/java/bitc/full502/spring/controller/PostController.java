package bitc.full502.spring.controller;

import bitc.full502.spring.dto.*;
import bitc.full502.spring.service.PostService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/posts")
public class PostController {

    private final PostService postService;

    // 목록(+검색)
    @GetMapping
    public PageResponse<PostDto.ListItem> list(
            @RequestParam(defaultValue = "") String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Page<PostDto.ListItem> p = postService.list(q, page, size);
        return PageResponse.of(p);
    }

    // 작성
    @PostMapping
    public Map<String, Object> create(@Valid @RequestBody PostDto.Create req) {
        Long id = postService.create(req);
        return Map.of("id", id);
    }

    // 상세 (조회수 증가)
    @GetMapping("/{id}")
    public PostDto.Detail detail(
            @PathVariable Long id,
            @RequestParam(required = false) Long me // 내 userId(선택): 좋아요 여부 계산용
    ) {
        return postService.detail(id, me);
    }

    // 수정 (작성자 본인만)
    @PutMapping("/{id}")
    public ResponseEntity<?> update(
            @PathVariable Long id,
            @RequestParam Long me, // 내 userId
            @Valid @RequestBody PostDto.Update req
    ) {
        postService.update(id, me, req);
        return ResponseEntity.ok(Map.of("updated", true));
    }

    // 삭제 (작성자 본인만)
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(
            @PathVariable Long id,
            @RequestParam Long me // 내 userId
    ) {
        postService.delete(id, me);
        return ResponseEntity.ok(Map.of("deleted", true));
    }

    // 좋아요 토글
    @PostMapping("/{id}/like")
    public Map<String, Object> toggleLike(
            @PathVariable Long id,
            @RequestParam Long me // 내 userId
    ) {
        boolean liked = postService.toggleLike(id, me);
        return Map.of("liked", liked);
    }

    // 댓글 목록
    @GetMapping("/{id}/comments")
    public PageResponse<CommDto.Item> comments(
            @PathVariable Long id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return PageResponse.of(postService.comments(id, page, size));
    }

    // 댓글 작성
    @PostMapping("/{id}/comments")
    public Map<String, Object> addComment(
            @PathVariable Long id,
            @Valid @RequestBody CommDto.Create req
    ) {
        Long commentId = postService.addComment(id, req);
        return Map.of("commentId", commentId);
    }

    // 댓글 삭제 (본인만)
    @DeleteMapping("/comments/{commentId}")
    public Map<String, Object> deleteComment(
            @PathVariable Long commentId,
            @RequestParam Long me
    ) {
        postService.deleteComment(commentId, me);
        return Map.of("deleted", true);
    }
}
