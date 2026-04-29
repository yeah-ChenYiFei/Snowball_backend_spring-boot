// service/impl/BookServiceImpl.java
package com.example.snowball.service.impl;
import com.example.snowball.dto.BookCreateDTO;
import com.example.snowball.entity.Book;
import com.example.snowball.repository.BookRepository;
import com.example.snowball.service.BookService;
import com.example.snowball.vo.BookVO;
import org.springframework.stereotype.Service;
import java.util.List;
@Service
public class BookServiceImpl implements BookService {
    private final BookRepository bookRepository;
    public BookServiceImpl(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }
    @Override
    public List<BookVO> getMyBooks(Long userId) {
        return bookRepository.findByUserIdOrderByPurchaseDateDesc(userId).stream().map(book -> {
            BookVO vo = new BookVO();
            vo.setId(book.getId());
            vo.setTitle(book.getTitle());
            vo.setAuthor(book.getAuthor());
            vo.setPrice(book.getPrice());
            vo.setPurchaseDate(book.getPurchaseDate());
            vo.setCoverUrl(book.getCoverUrl());
            return vo;
        }).toList();
    }
    @Override
    public BookVO addBook(Long userId, BookCreateDTO dto) {
        Book book = new Book();
        book.setUserId(userId);
        book.setTitle(dto.getTitle());
        book.setAuthor(dto.getAuthor());
        book.setPrice(dto.getPrice());
        book.setPurchaseDate(dto.getPurchaseDate());
        book.setCoverUrl(dto.getCoverUrl());
        book = bookRepository.save(book);

        BookVO vo = new BookVO();
        vo.setId(book.getId());
        vo.setTitle(book.getTitle());
        vo.setAuthor(book.getAuthor());
        vo.setPrice(book.getPrice());
        vo.setPurchaseDate(book.getPurchaseDate());
        vo.setCoverUrl(book.getCoverUrl());
        return vo;
    }
}