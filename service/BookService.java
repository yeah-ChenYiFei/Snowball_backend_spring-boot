// service/BookService.java
package com.example.snowball.service;
import com.example.snowball.dto.BookCreateDTO;
import com.example.snowball.vo.BookVO;
import java.util.List;
public interface BookService {
    List<BookVO> getMyBooks(Long userId);

    BookVO addBook(Long userId, BookCreateDTO dto);
}