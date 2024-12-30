package com.bbook.service;

import org.springframework.stereotype.Service;
import com.bbook.repository.BookRepository;

import lombok.RequiredArgsConstructor;

import com.bbook.entity.Book;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MainBookService {

  private final BookRepository bookRepository;

  public List<Book> getBooksByCategory(String main, String mid, String detail) {
    if (detail != null) {
      return bookRepository.findByMainCategoryAndMidCategoryAndDetailCategory(main, mid, detail);
    } else if (mid != null) {
      return bookRepository.findByMainCategoryAndMidCategory(main, mid);
    } else if (main != null) {
      return bookRepository.findByMainCategory(main);
    }
    return bookRepository.findAll();
  }
}