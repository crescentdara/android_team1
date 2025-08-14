package bitc.full502.spring.service;

import bitc.full502.spring.domain.entity.Comm;
import bitc.full502.spring.domain.entity.Post;
import bitc.full502.spring.domain.entity.Users;
import bitc.full502.spring.domain.repository.CommRepository;
import bitc.full502.spring.domain.repository.PostRepository;
import bitc.full502.spring.domain.repository.UsersRepository;
import bitc.full502.spring.dto.CommDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class CommServiceImpl implements CommService {

    private final CommRepository commRepository;
    private final PostRepository postRepository;
    private final UsersRepository usersRepository;

    private Users getTestUser() {
        return usersRepository.findByUsersId("testuser")
                .orElseThrow(() -> new IllegalStateException("testuser가 없습니다."));
    }

    @Override
    public List<CommDto> list(Long postId) {
        Post post = postRepository.findById(postId).orElseThrow();
        return commRepository.findByPostOrderByCreatedAtAsc(post).stream()
                .map(c -> CommDto.builder()
                        .id(c.getId())
                        .postId(postId)
                        .parentId(c.getParent() == null ? null : c.getParent().getId())
                        .author(c.getUser().getUsersId())
                        .content(c.getContent())
                        .createdAt(c.getCreatedAt())
                        .build())
                .toList();
    }

    @Override
    public Long write(Long postId, Long parentId, String content) {
        Post post = postRepository.findById(postId).orElseThrow();
        Users user = getTestUser();
        Comm parent = (parentId == null) ? null : commRepository.findById(parentId).orElse(null);
        Comm c = Comm.builder().post(post).user(user).parent(parent).content(content).build();
        return commRepository.save(c).getId();
    }

    @Override
    public void edit(Long commId, String content) {
        Comm c = commRepository.findById(commId).orElseThrow();
        if (!"testuser".equals(c.getUser().getUsersId())) {
            throw new IllegalStateException("본인 댓글만 수정할 수 있습니다.");
        }
        c.setContent(content);
    }

    @Override
    public void remove(Long commId) {
        Comm c = commRepository.findById(commId).orElseThrow();
        if (!"testuser".equals(c.getUser().getUsersId())) {
            throw new IllegalStateException("본인 댓글만 삭제할 수 있습니다.");
        }
        // 자식(대댓글)부터 재귀 삭제 후 본인 삭제
        deleteChildren(c);
        commRepository.delete(c);
    }

    private void deleteChildren(Comm parent) {
        List<Comm> children = commRepository.findByParent(parent);
        for (Comm child : children) {
            deleteChildren(child);
        }
        if (!children.isEmpty()) {
            commRepository.deleteAll(children);
        }
    }
}
