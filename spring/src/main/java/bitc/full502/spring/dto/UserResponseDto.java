package bitc.full502.spring.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class UserResponseDto {
    private String usersId;
    private String name;
    private String email;
    private String phone;
}
