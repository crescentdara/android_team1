package bitc.full502.spring.domain.repository;

import bitc.full502.spring.domain.entity.Post;
import bitc.full502.spring.domain.entity.Users;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PostRepository extends JpaRepository<Post, Long> {

    Page<Post> findAllByOrderByCreatedAtDesc(Pageable pageable);

    // 🔎 검색용
    Page<Post> findByTitleContainingIgnoreCaseOrderByCreatedAtDesc(String q, Pageable pageable);
    Page<Post> findByContentContainingIgnoreCaseOrderByCreatedAtDesc(String q, Pageable pageable);
    Page<Post> findByUser_UsersIdContainingIgnoreCaseOrderByCreatedAtDesc(String q, Pageable pageable);

    // ✅ 내가 쓴 글 (최신순)
    List<Post> findByUserOrderByCreatedAtDesc(Users user);
}
