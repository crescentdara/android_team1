package bitc.full502.spring.dto;

import lombok.*;
import java.time.LocalDateTime;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class PostDto {
    private Long id;
    private String title;
    private String content;
    private String imgUrl;
    private Long lookCount;
    private long likeCount;
    private String author;
    private Boolean liked;           // 로그인 사용자가 좋아요 눌렀는지
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
