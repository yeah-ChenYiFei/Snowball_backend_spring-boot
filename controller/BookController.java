package com.example.snowball.controller;
import com.example.snowball.common.Result;
import com.example.snowball.entity.Book;
import com.example.snowball.repository.BookRepository;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.List;
@RestController
@RequestMapping("/api/v1/books")
public class BookController {
    private final BookRepository bookRepository;
    public BookController(BookRepository bookRepository) { this.bookRepository = bookRepository; }

    @GetMapping
    public Result<List<Book>> getMyBooks(Authentication auth) {
        Long userId = (Long) auth.getPrincipal();
        return Result.success(bookRepository.findByUserIdOrderByPurchaseDateDesc(userId));
    }

    @PostMapping
    public Result<Book> addBook(@RequestBody Book book, Authentication auth) {
        Long userId = (Long) auth.getPrincipal();
        book.setUserId(userId);
        return Result.success(bookRepository.save(book));
    }
}
