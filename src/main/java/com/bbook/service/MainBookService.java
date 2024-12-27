package com.bbook.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.bbook.entity.Book;
import com.bbook.repository.BookRepository;
import com.bbook.service.MainCategoryService;

import jakarta.persistence.EntityNotFoundException;

import com.bbook.constant.BookStatus;

import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;

@Service
@RequiredArgsConstructor
public class MainBookService {
  private final BookRepository bookRepository;
  private final MainCategoryService categoryService;

  public Map<Long, List<Book>> getBooksByTopLevelCategories() {
    List<Book> allBooks = bookRepository.findByBookStatus(BookStatus.SELL);

    Map<Long, List<Book>> groupedBooks = new HashMap<>();

    for (Book book : allBooks) {
      Long topLevelCategoryId = categoryService.findTopLevelCategoryId(book.getCategoryId());

      groupedBooks.computeIfAbsent(topLevelCategoryId, k -> new ArrayList<>())
          .add(book);
    }

    return groupedBooks;
  }
}
