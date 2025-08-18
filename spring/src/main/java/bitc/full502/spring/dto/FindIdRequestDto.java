package bitc.full502.spring.dto;


public class FindIdRequestDto {
    private String email;
    private String password;

    public FindIdRequestDto() {}

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}

