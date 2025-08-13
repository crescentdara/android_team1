package bitc.full502.spring.controller;

import bitc.full502.spring.dto.PostDto;
import bitc.full502.spring.service.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/posts")
@CrossOrigin // 안드로이드 에뮬레이터 접근 허용
public class PostApiController {
    private final PostService postService;

    @GetMapping
    public Page<PostDto> list(@RequestParam(defaultValue="0") int page,
                              @RequestParam(defaultValue="10") int size) {
        return postService.list(page, size);
    }

    @GetMapping("/{id}")
    public PostDto detail(@PathVariable Long id) {
        return postService.detail(id);
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Long create(@RequestPart("title") String title,
                       @RequestPart("content") String content,
                       @RequestPart(value = "image", required = false) MultipartFile image) throws IOException {
        return postService.create(title, content, image);
    }

    @PutMapping(path = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public void update(@PathVariable Long id,
                       @RequestPart("title") String title,
                       @RequestPart("content") String content,
                       @RequestPart(value = "image", required = false) MultipartFile image) throws IOException {
        postService.update(id, title, content, image);
    }

    @PostMapping("/{id}/like")
    public long toggleLike(@PathVariable Long id) {
        return postService.toggleLike(id);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        postService.delete(id);
    }

    @GetMapping("/search")
    public Page<PostDto> search(@RequestParam String field,
                                @RequestParam String q,
                                @RequestParam(defaultValue="0") int page,
                                @RequestParam(defaultValue="10") int size) {
        return postService.search(field, q, page, size);
    }


}
