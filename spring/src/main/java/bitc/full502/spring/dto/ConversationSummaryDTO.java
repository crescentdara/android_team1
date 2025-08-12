package bitc.full502.spring.dto;

import lombok.*;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConversationSummaryDTO {
    private String partnerId;
    private String roomId;
    private String lastContent;
    private Instant lastAt;
    private long unreadCount;
}
