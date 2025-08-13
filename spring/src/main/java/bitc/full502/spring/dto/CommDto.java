package bitc.full502.spring.dto;

import bitc.full502.spring.domain.entity.Comm;
import bitc.full502.spring.domain.entity.Users;

import jakarta.validation.constraints.NotBlank;
import java.time.LocalDateTime;

public class CommDto {

    public record Create(
            Long userId,
            @NotBlank String content,
            Long parentId // 대댓글 시 부모 댓글 id (없으면 null)
    ) {}

    public record Item(
            Long id,
            Long userId,
            String userName,
            String content,
            Long parentId,
            LocalDateTime createdAt
    ) {
        public static Item from(Comm c) {
            Users u = c.getUser();
            return new Item(
                    c.getId(),
                    u != null ? u.getId() : null,
                    u != null ? u.getName() : null,
                    c.getContent(),
                    c.getParent() != null ? c.getParent().getId() : null,
                    c.getCreatedAt()
            );
        }
    }
}
