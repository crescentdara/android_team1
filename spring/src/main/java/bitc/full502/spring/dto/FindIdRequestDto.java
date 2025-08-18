package bitc.full502.spring.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FindIdRequestDto {
    private String email;
    private String pass; // ✅ 필드명이 'pass' 여야 합니다 (DB 필드와 맞추기)
}


