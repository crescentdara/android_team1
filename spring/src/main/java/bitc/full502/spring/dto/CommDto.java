package bitc.full502.spring.dto;

import lombok.*;
import java.time.LocalDateTime;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class CommDto {
    private Long id;
    private Long postId;
    private Long parentId;      // null 이면 상위 댓글
    private String author;      // usersId
    private String content;
    private LocalDateTime createdAt;

    // 👇 추가: 마이페이지 > 내가 쓴 댓글 목록 카드에 사용
    private String postTitle;   // 댓글이 달린 글 제목
    private String postImgUrl;  // 댓글이 달린 글 대표 이미지(썸네일)
}
