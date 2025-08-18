package bitc.full502.spring.dto;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class FindIdRequestDto {
    private String email; // entity: Users.email
    private String pass;  // entity: Users.pass
}
