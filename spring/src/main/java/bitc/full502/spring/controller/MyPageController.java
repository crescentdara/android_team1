package bitc.full502.spring.controller;

import bitc.full502.spring.domain.entity.Post;
import bitc.full502.spring.domain.entity.Users;
import bitc.full502.spring.domain.repository.CommRepository;
import bitc.full502.spring.domain.repository.PostLikeRepository;
import bitc.full502.spring.domain.repository.PostRepository;
import bitc.full502.spring.domain.repository.UsersRepository;
import bitc.full502.spring.dto.CommDto;
import bitc.full502.spring.dto.PostDto;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/mypage")
@CrossOrigin(origins = "*")
public class MyPageController {

    private final UsersRepository usersRepository;
    private final PostRepository postRepository;
    private final PostLikeRepository postLikeRepository;
    private final CommRepository commRepository;

    private Users getUserByPk(Long userPk) {
        return usersRepository.findById(userPk)
                .orElseThrow(() -> new IllegalArgumentException("invalid userPk"));
    }

    /** 1) 내가 쓴 글 */
    @GetMapping("/posts")
    public List<PostDto> myPosts(@RequestParam("userPk") Long userPk) {
        Users user = getUserByPk(userPk);
        return postRepository.findByUserOrderByCreatedAtDesc(user).stream()
                .map(p -> PostDto.builder()
                        .id(p.getId())
                        .title(p.getTitle())
                        .content(p.getContent())
                        .imgUrl(p.getImg() == null ? null : "/" + p.getImg().replace("\\", "/"))
                        .likeCount(postLikeRepository.countByPost(p))
                        .lookCount(p.getLookCount() == null ? 0L : p.getLookCount())
                        .author(p.getUser().getUsersId())
                        .liked(false)
                        .createdAt(p.getCreatedAt())
                        .updatedAt(p.getUpdatedAt())
                        .build())
                .toList();
    }

    /** 2) 내가 쓴 댓글 */
    @GetMapping("/comments")
    public List<CommDto> myComments(@RequestParam("userPk") Long userPk) {
        Users user = getUserByPk(userPk);
        return commRepository.findByUserOrderByCreatedAtDesc(user).stream()
                .map(c -> CommDto.builder()
                        .id(c.getId())
                        .postId(c.getPost().getId())
                        .parentId(c.getParent() == null ? null : c.getParent().getId())
                        .author(c.getUser().getUsersId())
                        .content(c.getContent())
                        .createdAt(c.getCreatedAt())
                        .build())
                .toList();
    }

    /** 3) 좋아요 한 게시글 */
    @GetMapping("/liked-posts")
    public List<PostDto> likedPosts(@RequestParam("userPk") Long userPk) {
        Users user = getUserByPk(userPk);
        return postLikeRepository.findByUserOrderByPostCreatedAtDesc(user).stream()
                .map(pl -> {
                    Post p = pl.getPost();
                    return PostDto.builder()
                            .id(p.getId())
                            .title(p.getTitle())
                            .content(p.getContent())
                            .imgUrl(p.getImg() == null ? null : "/" + p.getImg().replace("\\", "/"))
                            .likeCount(postLikeRepository.countByPost(p))
                            .lookCount(p.getLookCount() == null ? 0L : p.getLookCount())
                            .author(p.getUser().getUsersId())
                            .liked(true)
                            .createdAt(p.getCreatedAt())
                            .updatedAt(p.getUpdatedAt())
                            .build();
                })
                .toList();
    }
}
