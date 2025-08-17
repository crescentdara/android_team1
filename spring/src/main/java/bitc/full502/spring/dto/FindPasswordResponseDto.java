package bitc.full502.spring.dto;

public class FindPasswordResponseDto {
    private String password;

    public FindPasswordResponseDto(String password) {
        this.password = password;
    }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}
