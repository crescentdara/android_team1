package bitc.full502.spring.domain.repository;

import bitc.full502.spring.domain.entity.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostRepository extends JpaRepository<Post, Long> {
    Page<Post> findAllByOrderByCreatedAtDesc(Pageable pageable);

    // 🔎 검색용
    Page<Post> findByTitleContainingIgnoreCaseOrderByCreatedAtDesc(String q, Pageable pageable);
    Page<Post> findByContentContainingIgnoreCaseOrderByCreatedAtDesc(String q, Pageable pageable);
    Page<Post> findByUser_UsersIdContainingIgnoreCaseOrderByCreatedAtDesc(String q, Pageable pageable);
}
