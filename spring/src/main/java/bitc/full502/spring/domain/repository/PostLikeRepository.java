package bitc.full502.spring.domain.repository;

import bitc.full502.spring.domain.entity.Post;
import bitc.full502.spring.domain.entity.PostLike;
import bitc.full502.spring.domain.entity.Users;
import org.springframework.data.jpa.repository.*;

public interface PostLikeRepository extends JpaRepository<PostLike, Long> {

    boolean existsByUserAndPost(Users user, Post post);

    long countByPost(Post post);

    @Modifying
    @Query("DELETE FROM PostLike pl WHERE pl.user = :user AND pl.post = :post")
    void deleteByUserAndPost(Users user, Post post);
}
