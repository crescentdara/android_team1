package bitc.full502.spring.domain.repository;

import bitc.full502.spring.domain.entity.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

public interface PostRepository extends JpaRepository<Post, Long> {

    // 검색: 제목 or 내용 부분 일치(대소문자 무시)
    Page<Post> findByTitleContainingIgnoreCaseOrContentContainingIgnoreCase(
            String titleQ, String contentQ, Pageable pageable
    );

    // 조회수 +1
    @Modifying
    @Query("UPDATE Post p SET p.lookCount = COALESCE(p.lookCount,0) + 1 WHERE p.id = :postId")
    void incrementView(@Param("postId") Long postId);
}
