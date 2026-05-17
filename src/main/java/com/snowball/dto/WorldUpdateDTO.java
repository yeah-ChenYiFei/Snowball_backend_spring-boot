package com.snowball.dto;

import lombok.Data;

@Data
public class WorldUpdateDTO {
    private String name;
    private String description;
    private String type;
    private Boolean isPublic;
}
