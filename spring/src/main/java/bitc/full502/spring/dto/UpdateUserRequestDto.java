package bitc.full502.spring.dto;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class UpdateUserRequestDto {
    private String usersId; // 식별자
    private String name;
    private String email;
    private String phone;
}
