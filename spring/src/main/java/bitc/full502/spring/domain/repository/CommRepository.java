package bitc.full502.spring.domain.repository;

import bitc.full502.spring.domain.entity.Comm;
import bitc.full502.spring.domain.entity.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommRepository extends JpaRepository<Comm, Long> {
    Page<Comm> findByPostOrderByCreatedAtAsc(Post post, Pageable pageable);
}
