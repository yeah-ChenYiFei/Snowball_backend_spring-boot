package com.snowball.controller;

import com.snowball.common.Result;
import com.snowball.dto.BookCreateDTO;
import com.snowball.service.BookService;
import com.snowball.vo.BookVO;
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

    @PutMapping("/{id}")
    public Result<BookVO> updateBook(@PathVariable Long id, @RequestBody BookCreateDTO dto) {
        return Result.success(bookService.updateBook(id, getCurrentUserId(), dto));
    }

    @DeleteMapping("/{id}")
    public Result<String> deleteBook(@PathVariable Long id) {
        bookService.deleteBook(id, getCurrentUserId());
        return Result.success("删除成功");
    }
}
