package bitc.full502.spring.service;

import bitc.full502.spring.domain.entity.*;
import bitc.full502.spring.domain.repository.*;
import bitc.full502.spring.dto.PostDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class PostService {
    private final PostRepository postRepository;
    private final UsersRepository usersRepository;
    private final PostLikeRepository postLikeRepository;
    private final FileStorage fileStorage;
    private final CommRepository commRepository;

    private Users getUserOrThrow(String usersId) {
        return usersRepository.findByUsersId(usersId)
                .orElseThrow(() -> new IllegalArgumentException("유효하지 않은 사용자입니다."));
    }

    public Page<PostDto> list(int page, int size) {
        Page<Post> result = postRepository.findAllByOrderByCreatedAtDesc(PageRequest.of(page, size));
        return result.map(p -> PostDto.builder()
                .id(p.getId())
                .title(p.getTitle())
                .content(p.getContent())
                .imgUrl(p.getImg() == null ? null : "/"+p.getImg().replace("\\","/"))
                .lookCount(p.getLookCount() == null ? 0L : p.getLookCount())
                .likeCount(postLikeRepository.countByPost(p))
                .author(p.getUser().getUsersId())
                .liked(false) // 리스트에서는 굳이 계산 안 함 (필요시 계산 가능)
                .createdAt(p.getCreatedAt())
                .updatedAt(p.getUpdatedAt())
                .build());
    }

    public PostDto detail(Long id, String requesterId) {
        // 비로그인 접근 불가: 컨트롤러에서 선검사하지만, 방어적으로 체크
        if (requesterId == null || requesterId.isBlank()) {
            throw new IllegalStateException("로그인이 필요합니다.");
        }
        Users me = getUserOrThrow(requesterId);

        Post p = postRepository.findById(id).orElseThrow();
        p.setLookCount((p.getLookCount()==null?0:p.getLookCount()) + 1);

        boolean liked = postLikeRepository.findByUserAndPost(me, p).isPresent();

        return PostDto.builder()
                .id(p.getId())
                .title(p.getTitle())
                .content(p.getContent())
                .imgUrl(p.getImg()==null?null:"/"+p.getImg().replace("\\","/"))
                .lookCount(p.getLookCount())
                .likeCount(postLikeRepository.countByPost(p))
                .author(p.getUser().getUsersId())
                .liked(liked)
                .createdAt(p.getCreatedAt())
                .updatedAt(p.getUpdatedAt())
                .build();
    }

    public Long create(String title, String content, MultipartFile image, String requesterId) throws IOException {
        Users user = getUserOrThrow(requesterId);
        String saved = fileStorage.saveImage(image); // null 허용
        Post p = Post.builder()
                .title(title).content(content)
                .img(saved==null?null:"uploads/"+saved)
                .user(user).lookCount(0L)
                .build();
        return postRepository.save(p).getId();
    }

    public void update(Long id, String title, String content, MultipartFile image, String requesterId) throws IOException {
        Users user = getUserOrThrow(requesterId);
        Post p = postRepository.findById(id).orElseThrow();

        if (!p.getUser().getUsersId().equals(user.getUsersId())) {
            throw new SecurityException("본인 글만 수정할 수 있습니다.");
        }

        p.setTitle(title);
        p.setContent(content);
        if (image != null && !image.isEmpty()) {
            String saved = fileStorage.saveImage(image);
            p.setImg("uploads/"+saved);
        }
    }

    public long toggleLike(Long postId, String requesterId) {
        Users user = getUserOrThrow(requesterId);
        Post post = postRepository.findById(postId).orElseThrow();

        postLikeRepository.findByUserAndPost(user, post).ifPresentOrElse(
                postLikeRepository::delete,
                () -> postLikeRepository.save(PostLike.builder().user(user).post(post).build())
        );
        return postLikeRepository.countByPost(post);
    }

    @Transactional
    public void delete(Long id, String requesterId) {
        Users user = getUserOrThrow(requesterId);
        Post p = postRepository.findById(id).orElseThrow();

        if (!p.getUser().getUsersId().equals(user.getUsersId())) {
            throw new SecurityException("본인 글만 삭제할 수 있습니다.");
        }

        // 1) 좋아요 삭제
        postLikeRepository.deleteByPost(p);

        // 2) 댓글/대댓글 삭제
        commRepository.deleteByPostAndParentIsNotNull(p);
        commRepository.deleteByPostAndParentIsNull(p);

        // 3) 게시글 삭제
        postRepository.delete(p);
    }

    public Page<PostDto> search(String field, String q, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Post> result;

        switch ((field==null?"":field).toLowerCase()) {
            case "title" -> result = postRepository.findByTitleContainingIgnoreCaseOrderByCreatedAtDesc(q, pageable);
            case "content" -> result = postRepository.findByContentContainingIgnoreCaseOrderByCreatedAtDesc(q, pageable);
            case "author" -> result = postRepository.findByUser_UsersIdContainingIgnoreCaseOrderByCreatedAtDesc(q, pageable);
            default -> result = postRepository.findAllByOrderByCreatedAtDesc(pageable);
        }
        return result.map(p -> PostDto.builder()
                .id(p.getId())
                .title(p.getTitle())
                .content(p.getContent())
                .imgUrl(p.getImg()==null?null:"/"+p.getImg().replace("\\","/"))
                .lookCount(p.getLookCount()==null?0L:p.getLookCount())
                .likeCount(postLikeRepository.countByPost(p))
                .author(p.getUser().getUsersId())
                .liked(false)
                .createdAt(p.getCreatedAt())
                .updatedAt(p.getUpdatedAt())
                .build());
    }
}
