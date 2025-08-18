package bitc.full502.spring.dto;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class LoginRequestDto {
    private String usersId; // entity: Users.usersId
    private String pass;    // entity: Users.pass
}
