package com.snowball.dto;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
@Data
public class DeleteAccountDTO {
    @NotBlank(message = "密码不能为空")
    private String password;
}
