// dto/BookCreateDTO.java
package com.example.snowball.dto;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
@Data
public class BookCreateDTO {
    private String title;
    private String author;
    private BigDecimal price;
    private LocalDate purchaseDate;
    private String coverUrl;
}