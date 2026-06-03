package com.snowball.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class WorldRelationCreateDTO {
    @NotNull(message = "请选择第一个设定")
    private Long fromEntryId;

    @NotNull(message = "请选择第二个设定")
    private Long toEntryId;

    @NotBlank(message = "请选择箭头方向")
    private String direction; // LEFT_ARROW, RIGHT_ARROW, BIDIRECTIONAL

    private String description;

    // Optional: list of all entry IDs for multi-entry relations (>2 entries)
    // When provided with >2 entries, a circle group is created on the graph
    private java.util.List<Long> entryIds;
}
