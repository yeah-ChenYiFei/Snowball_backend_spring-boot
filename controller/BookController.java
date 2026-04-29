package com.example.snowball.controller;

import com.example.snowball.common.Result;
import com.example.snowball.dto.BookCreateDTO;
import com.example.snowball.service.BookService;
import com.example.snowball.vo.BookVO;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/books")
public class BookController extends BaseController {

    private final BookService bookService;

    public BookController(BookService bookService) {
        this.bookService = bookService;
    }

    @GetMapping
    public Result<List<BookVO>> getMyBooks() {
        return Result.success(bookService.getMyBooks(getCurrentUserId()));
    }

    @PostMapping
    public Result<BookVO> addBook(@RequestBody BookCreateDTO dto) {
        return Result.success(bookService.addBook(getCurrentUserId(), dto));
    }
}
