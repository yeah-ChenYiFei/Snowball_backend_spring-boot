package com.snowball.dto;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
@Data
public class VerifyEmailDTO {
    @NotBlank(message = "验证码不能为空")
    private String code;
}
