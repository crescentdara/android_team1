package bitc.full502.spring.dto;

import lombok.*;
import java.time.LocalDateTime;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class PostDto {
    private Long id;
    private String title;
    private String content;
    private String imgUrl;     // 정적 URL (/uploads/파일명)
    private Long lookCount;
    private long likeCount;
    private String author;     // usersId
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
