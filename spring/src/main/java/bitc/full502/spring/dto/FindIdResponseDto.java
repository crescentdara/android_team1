package bitc.full502.spring.dto;

public class FindIdResponseDto {
    private String userId;

    public FindIdResponseDto(String userId) {
        this.userId = userId;
    }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
}
