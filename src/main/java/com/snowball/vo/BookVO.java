// vo/BookVO.java
package com.snowball.vo;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
@Data
public class BookVO {
    private Long id;
    private String title;
    private String author;
    private BigDecimal price;
    private LocalDate purchaseDate;
    private String coverUrl;
}