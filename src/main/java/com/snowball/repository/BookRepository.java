package com.snowball.repository;
import com.snowball.entity.Book;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface BookRepository extends JpaRepository<Book, Long> {
    List<Book> findByUserIdOrderByPurchaseDateDesc(Long userId);
}
