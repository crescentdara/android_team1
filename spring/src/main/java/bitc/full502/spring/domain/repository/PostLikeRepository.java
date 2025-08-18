package bitc.full502.spring.domain.repository;

import bitc.full502.spring.domain.entity.Post;
import bitc.full502.spring.domain.entity.PostLike;
import bitc.full502.spring.domain.entity.Users;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PostLikeRepository extends JpaRepository<PostLike, Long> {
    long countByPost(Post post);
    Optional<PostLike> findByUserAndPost(Users user, Post post);

    // 🔧 추가: 게시글 기준 일괄 삭제
    long deleteByPost(Post post);
}

