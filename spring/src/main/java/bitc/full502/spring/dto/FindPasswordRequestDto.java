package bitc.full502.spring.dto;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class FindPasswordRequestDto {
    private String usersId; // entity: Users.usersId
    private String email;   // entity: Users.email
}
