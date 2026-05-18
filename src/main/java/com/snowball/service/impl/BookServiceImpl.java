// service/impl/BookServiceImpl.java
package com.snowball.service.impl;
import com.snowball.dto.BookCreateDTO;
import com.snowball.entity.Book;
import com.snowball.repository.BookRepository;
import com.snowball.service.BookService;
import com.snowball.vo.BookVO;
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
            vo.setUserId(book.getUserId());
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
        vo.setUserId(book.getUserId());
        vo.setTitle(book.getTitle());
        vo.setAuthor(book.getAuthor());
        vo.setPrice(book.getPrice());
        vo.setPurchaseDate(book.getPurchaseDate());
        vo.setCoverUrl(book.getCoverUrl());
        return vo;
    }

    @Override
    public BookVO updateBook(Long id, Long userId, BookCreateDTO dto) {
        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("图书记录不存在"));
        if (!book.getUserId().equals(userId)) {
            throw new RuntimeException("只能修改自己的图书记录");
        }
        book.setTitle(dto.getTitle());
        book.setAuthor(dto.getAuthor());
        book.setPrice(dto.getPrice());
        book.setPurchaseDate(dto.getPurchaseDate());
        book.setCoverUrl(dto.getCoverUrl());
        bookRepository.save(book);

        BookVO vo = new BookVO();
        vo.setId(book.getId());
        vo.setUserId(book.getUserId());
        vo.setTitle(book.getTitle());
        vo.setAuthor(book.getAuthor());
        vo.setPrice(book.getPrice());
        vo.setPurchaseDate(book.getPurchaseDate());
        vo.setCoverUrl(book.getCoverUrl());
        return vo;
    }

    @Override
    public void deleteBook(Long id, Long userId) {
        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("图书记录不存在"));
        if (!book.getUserId().equals(userId)) {
            throw new RuntimeException("只能删除自己的图书记录");
        }
        bookRepository.delete(book);
    }
}