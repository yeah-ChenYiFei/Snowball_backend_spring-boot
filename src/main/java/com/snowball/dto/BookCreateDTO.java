// dto/BookCreateDTO.java
package com.snowball.dto;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
@Data
public class BookCreateDTO {
    @NotBlank(message = "书名不能为空")
    @Size(max = 200, message = "书名最长200字")
    private String title;
    @Size(max = 100, message = "作者名最长100字")
    private String author;
    private BigDecimal price;
    private LocalDate purchaseDate;
    private String coverUrl;
}
