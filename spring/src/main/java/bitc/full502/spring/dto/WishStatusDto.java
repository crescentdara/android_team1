package bitc.full502.spring.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data @NoArgsConstructor @AllArgsConstructor
public class WishStatusDto {
    private boolean wished;
    private long wishCount;
}
