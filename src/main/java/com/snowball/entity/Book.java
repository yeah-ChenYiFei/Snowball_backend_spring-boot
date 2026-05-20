package com.snowball.entity;
import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
@Data
@Entity
@Table(name = "books", indexes = {
    @Index(name = "idx_books_user_id", columnList = "user_id")
})
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
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
