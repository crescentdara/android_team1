package bitc.full502.spring.dto;

import lombok.*;
import java.time.LocalDateTime;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class CommDto {
    private Long id;
    private Long postId;
    private Long parentId;    // null 이면 상위 댓글
    private String author;    // usersId
    private String content;
    private LocalDateTime createdAt;
}
