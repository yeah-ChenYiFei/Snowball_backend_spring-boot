// dto/RevisionCreateDTO.java
package com.example.snowball.dto;
import lombok.Data;
@Data
public class RevisionCreateDTO {
    private String title;
    private String body;
    private String summary;
}