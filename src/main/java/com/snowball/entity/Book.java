package com.snowball.entity;
import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
@Data
@Entity
@Table(name = "books")
public class Book {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "user_id", nullable = false)
    private Long userId;
    @Column(length = 200, nullable = false)
    private String title;
    @Column(length = 100)
    private String author;
    private BigDecimal price;
    @Column(name = "purchase_date")
    private LocalDate purchaseDate;
    @Column(name = "cover_url", length = 500)
    private String coverUrl;
}
