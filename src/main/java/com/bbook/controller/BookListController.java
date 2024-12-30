package com.bbook.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import com.bbook.dto.BookListDto;
import com.bbook.service.BookListService;

import lombok.RequiredArgsConstructor;
import java.util.List;

@Controller
@RequestMapping("/book-list")
@RequiredArgsConstructor
public class BookListController {

  private final BookListService bookListService;

  // 도서 목록 페이지 (메인)
  @GetMapping
  public String bookList(
      @RequestParam(defaultValue = "10") int size,
      Model model) {
    List<BookListDto> latestBooks = bookListService.getInitialBooks(size);
    model.addAttribute("books", latestBooks);
    model.addAttribute("pageTitle", "전체 도서");
    return "books/bookList";
  }

  // 메스트셀러 도서 목록
  @GetMapping("/best")
  public String bestBooks(
      @RequestParam(defaultValue = "10") int size,
      Model model) {
    List<BookListDto> bestBooks = bookListService.getInitialBestBooks(size);
    model.addAttribute("books", bestBooks);
    model.addAttribute("pageTitle", "베스트셀러");
    model.addAttribute("category", "best");
    return "books/bookList";
  }

  // 신간 도서 목록
  @GetMapping("/new")
  public String newBooks(
      @RequestParam(defaultValue = "10") int size,
      Model model) {
    List<BookListDto> newBooks = bookListService.getInitialNewBooks(size);
    model.addAttribute("books", newBooks);
    model.addAttribute("pageTitle", "신간 도서");
    model.addAttribute("category", "new");
    return "books/bookList";
  }

  // 카테고리별 도서 목록 (메인/중분류/상세 통합)
  @GetMapping("/category")
  public String booksByCategory(
      @RequestParam(required = false) String main,
      @RequestParam(required = false) String mid,
      @RequestParam(required = false) String detail,
      @RequestParam(required = false, defaultValue = "newest") String sort,
      @RequestParam(defaultValue = "10") int size,
      Model model) {
    try {
      List<BookListDto> books;

      if (detail != null) {
        // 상세 카테고리가 있는 경우
        books = bookListService.getInitialBooksByDetailCategory(main, mid, detail, size);
        model.addAttribute("pageTitle", detail);
        model.addAttribute("currentDetailCategory", detail);
      } else if (mid != null) {
        // 중분류 카테고리만 있는 경우
        books = bookListService.getInitialBooksByCategory(main, mid, size);
        model.addAttribute("pageTitle", mid);
      } else if (main != null) {
        // 메인 카테고리만 있는 경우
        books = bookListService.getInitialBooksByMainCategory(main, size);
        model.addAttribute("pageTitle", main);
      } else {
        // 카테고리가 없는 경우 전체 도서 목록
        books = bookListService.getInitialBooks(size);
        model.addAttribute("pageTitle", "전체 도서");
      }

      model.addAttribute("books", books);
      model.addAttribute("currentMainCategory", main);
      model.addAttribute("currentMidCategory", mid);

      // 현재 카테고리의 하위 카테고리 목록 추가
      if (main != null) {
        List<String> midCategories = bookListService.getMidCategories(main);
        model.addAttribute("midCategories", midCategories);

        if (mid != null) {
          List<String> detailCategories = bookListService.getDetailCategories(main, mid);
          model.addAttribute("detailCategories", detailCategories);
        }
      }

      return "books/bookList";

    } catch (Exception e) {
      // 에러 로깅
      e.printStackTrace();
      model.addAttribute("error", "카테고리 조회 중 오류가 발생했습니다.");
      return "error/500"; // 에러 페이지로 리다이렉트
    }
  }

  // 무한 스크롤용 API 수정
  @GetMapping("/api/next")
  @ResponseBody
  public List<BookListDto> getNextBooks(
      @RequestParam Long lastId,
      @RequestParam(required = false) String category,
      @RequestParam(required = false) String main,
      @RequestParam(required = false) String mid,
      @RequestParam(required = false) String detail,
      @RequestParam(defaultValue = "10") int size) {

    if ("best".equals(category)) {
      return bookListService.getNextBestBooks(lastId, size);
    } else if ("new".equals(category)) {
      return bookListService.getNextNewBooks(lastId, size);
    } else if (detail != null) {
      return bookListService.getNextBooksByDetailCategory(lastId, main, mid, detail, size);
    } else if (mid != null) {
      return bookListService.getNextBooksByCategory(lastId, main, mid, size);
    } else {
      return bookListService.getNextBooksByMainCategory(lastId, main, size);
    }
  }

  // 도서 검색 기능
  @GetMapping("/search")
  public String searchBooks(
      @RequestParam String searchQuery,
      @RequestParam(required = false, defaultValue = "newest") String sort,
      @RequestParam(defaultValue = "10") int size,
      Model model) {
    try {
      List<BookListDto> searchResults = bookListService.searchInitialBooks(searchQuery, size);

      // 정렬 적용
      switch (sort) {
        case "price_asc":
          searchResults = bookListService.sortByPriceAsc(searchResults);
          break;
        case "price_desc":
          searchResults = bookListService.sortByPriceDesc(searchResults);
          break;
        case "popularity":
          searchResults = bookListService.sortByPopularity(searchResults);
          break;
        default: // newest
          // 이미 최신순으로 정렬되어 있으므로 추가 작업 불필요
          break;
      }

      model.addAttribute("books", searchResults);
      model.addAttribute("pageTitle", "\"" + searchQuery + "\" 검색 결과");
      model.addAttribute("searchQuery", searchQuery);
      model.addAttribute("currentSort", sort);
      model.addAttribute("selectedSort", sort);
      return "books/bookList";

    } catch (Exception e) {
      e.printStackTrace();
      model.addAttribute("error", "검색 중 오류가 발생했습니다.");
      return "error/500";
    }
  }

  // 검색 결과 무한 스크롤용 API 수정
  @GetMapping("/api/search/next")
  @ResponseBody
  public List<BookListDto> getNextSearchResults(
      @RequestParam Long lastId,
      @RequestParam String searchQuery,
      @RequestParam(required = false, defaultValue = "newest") String sort,
      @RequestParam(defaultValue = "10") int size) {

    List<BookListDto> nextResults = bookListService.getNextSearchResults(lastId, searchQuery, size);

    // 정렬 적용
    switch (sort) {
      case "price_asc":
        return bookListService.sortByPriceAsc(nextResults);
      case "price_desc":
        return bookListService.sortByPriceDesc(nextResults);
      case "popularity":
        return bookListService.sortByPopularity(nextResults);
      default: // newest
        return nextResults;
    }
  }
}