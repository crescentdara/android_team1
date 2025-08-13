package bitc.full502.spring.controller;

import bitc.full502.spring.dto.CommDto;
import bitc.full502.spring.service.CommService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/comments")
@CrossOrigin
public class CommApiController {

    private final CommService commService;  // ✅ 철자 수정

    @GetMapping("/{postId}")
    public List<CommDto> list(@PathVariable Long postId) {
        return commService.list(postId);
    }

    @PostMapping
    public Long write(@RequestParam Long postId,
                      @RequestParam(required = false) Long parentId,
                      @RequestParam String content) {
        return commService.write(postId, parentId, content);
    }

    @PutMapping("/{id}")
    public void edit(@PathVariable Long id,
                     @RequestParam String content) {
        commService.edit(id, content);
    }

    @DeleteMapping("/{id}")
    public void remove(@PathVariable Long id) {
        commService.remove(id);
    }
}
