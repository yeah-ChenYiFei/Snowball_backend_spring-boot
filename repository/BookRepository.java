package com.example.snowball.repository;
import com.example.snowball.entity.Book;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface BookRepository extends JpaRepository<Book, Long> {
    List<Book> findByUserIdOrderByPurchaseDateDesc(Long userId);
}
