package bitc.full502.spring.dto;

public class SignupRequestDto {
    private String name;
    private String usersId;
    private String pass;
    private String email;
    private String phone;

    public SignupRequestDto() {}

    public SignupRequestDto(String name, String usersId, String pass, String email, String phone) {
        this.name = name;
        this.usersId = usersId;
        this.pass = pass;
        this.email = email;
        this.phone = phone;
    }

    // Getter, Setter 모두 작성
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getUsersId() { return usersId; }
    public void setUsersId(String usersId) { this.usersId = usersId; }

    public String getPass() { return pass; }
    public void setPass(String pass) { this.pass = pass; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
}

