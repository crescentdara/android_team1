package bitc.full502.spring.controller;

import bitc.full502.spring.dto.PostDto;
import bitc.full502.spring.service.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/posts")
public class PostApiController {

    private final PostService postService;

    private String requireUser(String usersId) {
        if (usersId == null || usersId.isBlank())
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "X-USER-ID header required");
        return usersId;
    }

    @GetMapping
    public Page<PostDto> list(@RequestParam(defaultValue = "0") int page,
                              @RequestParam(defaultValue = "20") int size) {
        return postService.list(page, size);
    }

    @GetMapping("/{id}")
    public PostDto detail(@PathVariable Long id,
                          @RequestHeader(value = "X-USER-ID", required = false) String usersId) {
        return postService.detail(id, requireUser(usersId));
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Long create(@RequestPart("title") String title,
                       @RequestPart("content") String content,
                       @RequestPart(value = "image", required = false) MultipartFile image,
                       @RequestHeader(value = "X-USER-ID", required = false) String usersId) throws Exception {
        return postService.create(title, content, image, requireUser(usersId));
    }

    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Void> update(@PathVariable Long id,
                                       @RequestPart("title") String title,
                                       @RequestPart("content") String content,
                                       @RequestPart(value = "image", required = false) MultipartFile image,
                                       @RequestHeader(value = "X-USER-ID", required = false) String usersId) throws Exception {
        postService.update(id, title, content, image, requireUser(usersId));
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/like")
    public Long toggleLike(@PathVariable Long id,
                           @RequestHeader(value = "X-USER-ID", required = false) String usersId) {
        return postService.toggleLike(id, requireUser(usersId));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id,
                                       @RequestHeader(value = "X-USER-ID", required = false) String usersId) {
        postService.delete(id, requireUser(usersId));
        return ResponseEntity.ok().build();
    }

    @GetMapping("/search")
    public Page<PostDto> search(@RequestParam String field,
                                @RequestParam String q,
                                @RequestParam(defaultValue = "0") int page,
                                @RequestParam(defaultValue = "20") int size) {
        return postService.search(field, q, page, size);
    }
}
