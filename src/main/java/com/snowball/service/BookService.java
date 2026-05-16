// service/BookService.java
package com.snowball.service;
import com.snowball.dto.BookCreateDTO;
import com.snowball.vo.BookVO;
import java.util.List;
public interface BookService {
    List<BookVO> getMyBooks(Long userId);

    BookVO addBook(Long userId, BookCreateDTO dto);
}