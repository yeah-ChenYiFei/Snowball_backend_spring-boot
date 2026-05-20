package com.snowball.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class WorldChangeActionDTO {
    @NotBlank(message = "操作类型不能为空")
    private String action;    // APPROVE or REJECT
    @Size(max = 500, message = "驳回理由最长500字")
    private String rejectReason;
}
