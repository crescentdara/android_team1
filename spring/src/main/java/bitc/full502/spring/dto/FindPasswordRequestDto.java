package bitc.full502.spring.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FindPasswordRequestDto {
    private String usersId; // ✅ usersId 맞춤
    private String email;
}
