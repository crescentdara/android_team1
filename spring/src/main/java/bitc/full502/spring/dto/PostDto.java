package bitc.full502.spring.dto;

import bitc.full502.spring.domain.entity.Post;
import bitc.full502.spring.domain.entity.Users;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;

public class PostDto {

    // 요청 DTO
    public record Create(
            Long userId,
            @NotBlank @Size(max = 150) String title,
            String content,
            String img
    ) {}

    public record Update(
            @NotBlank @Size(max = 150) String title,
            String content,
            String img
    ) {}

    // 응답 DTO (리스트)
    public record ListItem(
            Long id,
            String title,
            String authorName,
            Long lookCount,
            LocalDateTime createdAt
    ) {
        public static ListItem from(Post p) {
            return new ListItem(
                    p.getId(),
                    p.getTitle(),
                    p.getUser() != null ? p.getUser().getName() : null,
                    p.getLookCount() != null ? p.getLookCount() : 0L,
                    p.getCreatedAt()
            );
        }
    }

    // 응답 DTO (상세)
    public record Detail(
            Long id,
            String title,
            String content,
            String img,
            Long lookCount,
            Long authorId,
            String authorName,
            LocalDateTime createdAt,
            LocalDateTime updatedAt,
            long likeCount,
            boolean likedByMe
    ) {
        public static Detail of(Post p, long likeCount, boolean likedByMe) {
            Users u = p.getUser();
            return new Detail(
                    p.getId(),
                    p.getTitle(),
                    p.getContent(),
                    p.getImg(),
                    p.getLookCount() != null ? p.getLookCount() : 0L,
                    u != null ? u.getId() : null,
                    u != null ? u.getName() : null,
                    p.getCreatedAt(),
                    p.getUpdatedAt(),
                    likeCount,
                    likedByMe
            );
        }
    }
}
