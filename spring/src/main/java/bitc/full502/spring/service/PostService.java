package bitc.full502.spring.service;

import bitc.full502.spring.domain.entity.*;
import bitc.full502.spring.domain.repository.UsersRepository;
import bitc.full502.spring.dto.*;
import bitc.full502.spring.exception.ApiException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import bitc.full502.spring.domain.repository.PostRepository;
import bitc.full502.spring.domain.repository.PostLikeRepository;
import bitc.full502.spring.domain.repository.CommRepository;
import bitc.full502.spring.domain.repository.UsersRepository;


@Service
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;
    private final PostLikeRepository postLikeRepository;
    private final CommRepository commRepository;
    private final UsersRepository usersRepository; // <== Users 조회용 (아래 인터페이스 함께 추가 안내)

    // 목록 + 검색
    public Page<PostDto.ListItem> list(String q, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Post> result;
        if (q == null || q.isBlank()) {
            result = postRepository.findAll(pageable);
        } else {
            result = postRepository.findByTitleContainingIgnoreCaseOrContentContainingIgnoreCase(q, q, pageable);
        }
        return result.map(PostDto.ListItem::from);
    }


    // 작성
    @Transactional
    public Long create(PostDto.Create req) {
        Users user = usersRepository.findById(req.userId())
                .orElseThrow(() -> new ApiException("작성자(userId) 없음"));
        Post p = Post.builder()
                .user(user)
                .title(req.title())
                .content(req.content())
                .img(req.img())
                .lookCount(0L)
                .build();
        return postRepository.save(p).getId();
    }

    // 상세 (조회수 +1)
    @Transactional
    public PostDto.Detail detail(Long postId, Long meUserIdOrNull) {
        postRepository.incrementView(postId);
        Post p = postRepository.findById(postId)
                .orElseThrow(() -> new ApiException("게시글 없음"));
        long likeCount = postLikeRepository.countByPost(p);
        boolean likedByMe = false;
        if (meUserIdOrNull != null) {
            Users me = usersRepository.findById(meUserIdOrNull)
                    .orElseThrow(() -> new ApiException("사용자 없음"));
            likedByMe = postLikeRepository.existsByUserAndPost(me, p);
        }
        return PostDto.Detail.of(p, likeCount, likedByMe);
    }

    // 수정
    @Transactional
    public void update(Long postId, Long userId, PostDto.Update req) {
        Post p = postRepository.findById(postId)
                .orElseThrow(() -> new ApiException("게시글 없음"));
        if (!p.getUser().getId().equals(userId)) {
            throw new ApiException("작성자만 수정 가능");
        }
        p.setTitle(req.title());
        p.setContent(req.content());
        p.setImg(req.img());
        // JPA dirty checking
    }

    // 삭제
    @Transactional
    public void delete(Long postId, Long userId) {
        Post p = postRepository.findById(postId)
                .orElseThrow(() -> new ApiException("게시글 없음"));
        if (!p.getUser().getId().equals(userId)) {
            throw new ApiException("작성자만 삭제 가능");
        }
        postRepository.delete(p);
    }

    // 좋아요 토글
    @Transactional
    public boolean toggleLike(Long postId, Long userId) {
        Post p = postRepository.findById(postId)
                .orElseThrow(() -> new ApiException("게시글 없음"));
        Users u = usersRepository.findById(userId)
                .orElseThrow(() -> new ApiException("사용자 없음"));

        boolean exists = postLikeRepository.existsByUserAndPost(u, p);
        if (exists) {
            postLikeRepository.deleteByUserAndPost(u, p);
            return false; // 취소됨
        } else {
            PostLike like = PostLike.builder().user(u).post(p).build();
            postLikeRepository.save(like);
            return true; // 좋아요됨
        }
    }

    // 댓글 목록
    public Page<CommDto.Item> comments(Long postId, int page, int size) {
        Post p = postRepository.findById(postId)
                .orElseThrow(() -> new ApiException("게시글 없음"));
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "createdAt"));
        return commRepository.findByPostOrderByCreatedAtAsc(p, pageable)
                .map(CommDto.Item::from);
    }

    // 댓글 작성
    @Transactional
    public Long addComment(Long postId, CommDto.Create req) {
        Post p = postRepository.findById(postId)
                .orElseThrow(() -> new ApiException("게시글 없음"));
        Users u = usersRepository.findById(req.userId())
                .orElseThrow(() -> new ApiException("사용자 없음"));

        Comm parent = null;
        if (req.parentId() != null) {
            parent = commRepository.findById(req.parentId())
                    .orElseThrow(() -> new ApiException("부모 댓글 없음"));
        }

        Comm c = Comm.builder()
                .post(p)
                .user(u)
                .parent(parent)
                .content(req.content())
                .build();
        return commRepository.save(c).getId();
    }

    // 댓글 삭제 (본인만)
    @Transactional
    public void deleteComment(Long commentId, Long userId) {
        Comm c = commRepository.findById(commentId)
                .orElseThrow(() -> new ApiException("댓글 없음"));
        if (!c.getUser().getId().equals(userId)) {
            throw new ApiException("작성자만 삭제 가능");
        }
        commRepository.delete(c);
    }
}
