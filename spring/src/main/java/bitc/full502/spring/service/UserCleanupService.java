package bitc.full502.spring.service;

import bitc.full502.spring.domain.entity.*;
import bitc.full502.spring.domain.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserCleanupService {

    private final UsersRepository usersRepository;
    private final PostRepository postRepository;
    private final PostLikeRepository postLikeRepository;
    private final CommRepository commRepository;
    private final FlWishRepository flWishRepository;
    private final LodWishRepository lodWishRepository;
    private final FlBookRepository flBookRepository;
    private final LodBookRepository lodBookRepository;

    @Transactional
    public void cascadeDeleteByUsersId(String usersId) {
        Users user = usersRepository.findByUsersId(usersId)
                .orElseThrow(() -> new IllegalArgumentException("사용자 없음: " + usersId));

        // 1) 내가 누른 좋아요 → 게시글 단위/사용자 단위 모두 안전 삭제
        postLikeRepository.findByUserOrderByPostCreatedAtDesc(user)
                .forEach(postLikeRepository::delete);

        // 2) 내가 쓴 댓글 전부(자식댓글 포함) 삭제
        List<Comm> myComments = commRepository.findByUser(user);
        // 트리형이라 자식부터 날리거나, 재귀로 정리
        for (Comm c : myComments) {
            deleteCommentTree(c);
        }

        // 3) 내가 쓴 글(글의 댓글/좋아요 같이 제거)
        List<Post> myPosts = postRepository.findByUserOrderByCreatedAtDesc(user);
        for (Post p : myPosts) {
            // 댓글 전부
            List<Comm> comms = commRepository.findByPostOrderByCreatedAtAsc(p);
            for (Comm c : comms) deleteCommentTree(c);
            // 좋아요 전부
            postLikeRepository.deleteByPost(p);
            // 글 삭제
            postRepository.delete(p);
        }

        // 4) 항공/숙박 찜
        flWishRepository.findByUser_Id(user.getId()).forEach(flWishRepository::delete);
        // LodWish는 findByUser_Id 가 없다 → 사용자 기준 전체 삭제
        // (JPA 메서드 추가가 부담되면 아래 방식으로 일괄 조회 후 삭제)
        lodWishRepository.findAll().stream()
                .filter(w -> w.getUser().getId().equals(user.getId()))
                .forEach(lodWishRepository::delete);

        // 5) 항공/숙박 예약 (취소여부 무관하게 전부 제거)
        flBookRepository.findAll().stream()
                .filter(b -> b.getUser().getId().equals(user.getId()))
                .forEach(flBookRepository::delete);
        lodBookRepository.findAll().stream()
                .filter(b -> b.getUser().getId().equals(user.getId()))
                .forEach(lodBookRepository::delete);

        // 6) 마지막으로 사용자 삭제
        usersRepository.delete(user);
    }

    private void deleteCommentTree(Comm parent) {
        List<Comm> children = commRepository.findByParent(parent);
        for (Comm child : children) {
            deleteCommentTree(child);
        }
        if (!children.isEmpty()) {
            commRepository.deleteAll(children);
        }
        if (commRepository.existsById(parent.getId())) {
            commRepository.delete(parent);
        }
    }
}
