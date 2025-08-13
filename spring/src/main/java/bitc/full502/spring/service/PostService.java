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

@Service
@RequiredArgsConstructor
@Transactional
public class PostService {
    private final PostRepository postRepository;
    private final UsersRepository usersRepository;
    private final PostLikeRepository postLikeRepository;
    private final FileStorage fileStorage;
    private final CommRepository commRepository;

    private Users getTestUser() {
        return usersRepository.findByUsersId("testuser")
                .orElseThrow(() -> new IllegalStateException("testuser가 없습니다."));
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
                .createdAt(p.getCreatedAt())
                .updatedAt(p.getUpdatedAt())
                .build());
    }

    public PostDto detail(Long id) {
        Post p = postRepository.findById(id).orElseThrow();
        p.setLookCount((p.getLookCount()==null?0:p.getLookCount()) + 1);
        return PostDto.builder()
                .id(p.getId())
                .title(p.getTitle())
                .content(p.getContent())
                .imgUrl(p.getImg()==null?null:"/"+p.getImg().replace("\\","/"))
                .lookCount(p.getLookCount())
                .likeCount(postLikeRepository.countByPost(p))
                .author(p.getUser().getUsersId())
                .createdAt(p.getCreatedAt())
                .updatedAt(p.getUpdatedAt())
                .build();
    }

    public Long create(String title, String content, MultipartFile image) throws IOException {
        Users user = getTestUser();
        String saved = fileStorage.saveImage(image); // null 허용
        Post p = Post.builder()
                .title(title).content(content)
                .img(saved==null?null:"uploads/"+saved)
                .user(user).lookCount(0L)
                .build();
        return postRepository.save(p).getId();
    }

    public void update(Long id, String title, String content, MultipartFile image) throws IOException {
        Post p = postRepository.findById(id).orElseThrow();
        p.setTitle(title);
        p.setContent(content);
        if (image != null && !image.isEmpty()) {
            String saved = fileStorage.saveImage(image);
            p.setImg("uploads/"+saved);
        }
    }

    public long toggleLike(Long postId) {
        Users user = getTestUser();
        Post post = postRepository.findById(postId).orElseThrow();
        postLikeRepository.findByUserAndPost(user, post).ifPresentOrElse(
                postLikeRepository::delete,
                () -> postLikeRepository.save(PostLike.builder().user(user).post(post).build())
        );
        return postLikeRepository.countByPost(post);
    }

    @Transactional
    public void delete(Long id) {
        Post p = postRepository.findById(id).orElseThrow();

        // (선택) 글쓴이 검증: 로그인 전이라면 testuser만 허용
        if (!"testuser".equals(p.getUser().getUsersId())) {
            throw new IllegalStateException("본인 글만 삭제할 수 있습니다.");
        }

        // 1) 좋아요 삭제
        postLikeRepository.deleteByPost(p);


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
                .createdAt(p.getCreatedAt())
                .updatedAt(p.getUpdatedAt())
                .build());
    }



}
