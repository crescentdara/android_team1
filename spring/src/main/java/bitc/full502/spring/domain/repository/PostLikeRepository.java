package bitc.full502.spring.domain.repository;

import bitc.full502.spring.domain.entity.Post;
import bitc.full502.spring.domain.entity.PostLike;
import bitc.full502.spring.domain.entity.Users;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PostLikeRepository extends JpaRepository<PostLike, Long> {

    Optional<PostLike> findByUserAndPost(Users user, Post post);

    // 게시글 기준 일괄 삭제
    long deleteByPost(Post post);

    long countByPost(Post post);

    // ✅ 좋아요 목록: 좋아요한 게시글의 생성일(Post.createdAt) 내림차순
    List<PostLike> findByUserOrderByPostCreatedAtDesc(Users user);
}
