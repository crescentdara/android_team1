package bitc.full502.spring.service;

import bitc.full502.spring.domain.entity.*;
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

    private Users getUserOrThrow(String usersId) {
        return usersRepository.findByUsersId(usersId)
                .orElseThrow(() -> new IllegalArgumentException("유효하지 않은 사용자입니다."));
    }

    @Override
    @Transactional(readOnly = true)
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
    public Long write(Long postId, Long parentId, String content, String usersId) {
        Post post = postRepository.findById(postId).orElseThrow();
        Users user = getUserOrThrow(usersId);
        Comm parent = (parentId == null) ? null : commRepository.findById(parentId).orElse(null);
        Comm c = Comm.builder()
                .post(post)
                .user(user)
                .parent(parent)
                .content(content)
                .build();
        return commRepository.save(c).getId();
    }

    @Override
    public void edit(Long commId, String content, String usersId) {
        Users me = getUserOrThrow(usersId);
        Comm c = commRepository.findById(commId).orElseThrow();
        if (!me.getUsersId().equals(c.getUser().getUsersId())) {
            throw new SecurityException("본인 댓글만 수정할 수 있습니다.");
        }
        c.setContent(content);
    }

    @Override
    public void remove(Long commId, String usersId) {
        Users me = getUserOrThrow(usersId);
        Comm c = commRepository.findById(commId).orElseThrow();
        if (!me.getUsersId().equals(c.getUser().getUsersId())) {
            throw new SecurityException("본인 댓글만 삭제할 수 있습니다.");
        }
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
