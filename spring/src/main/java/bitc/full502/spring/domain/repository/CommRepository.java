package bitc.full502.spring.domain.repository;

import bitc.full502.spring.domain.entity.Comm;
import bitc.full502.spring.domain.entity.Post;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CommRepository extends JpaRepository<Comm, Long> {
    List<Comm> findByPostOrderByCreatedAtAsc(Post post);

     long deleteByPostAndParentIsNotNull(Post post); // 대댓글 먼저
     long deleteByPostAndParentIsNull(Post post);    // 부모댓글 다음

    long deleteByPost(Post post);

    // ✅ 대댓글 제거용
    List<Comm> findByParent(Comm parent);

}

