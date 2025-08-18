package bitc.full502.spring.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
public class SignupRequestDto {
    private String name;
    private String usersId;
    private String pass;
    private String email;
    private String phone;
}
