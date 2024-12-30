package com.bbook.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import lombok.RequiredArgsConstructor;
import com.bbook.entity.Book;
import com.bbook.service.MainBookService;
import com.bbook.service.MainCategoryService;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class MainController {

	private final MainCategoryService mainCategoryService;
	private final MainBookService mainBookService;

	@GetMapping("/")
	public String main(Model model) {
		// 메인 카테고리 목록 조회
		List<String> mainCategories = mainCategoryService.getMainCategories();
		model.addAttribute("mainCategories", mainCategories);

		return "main";
	}

	// 중간 카테고리 조회 API
	@GetMapping("/api/categories/{mainCategory}/mid")
	@ResponseBody
	public ResponseEntity<List<String>> getMidCategories(@PathVariable String mainCategory) {
		try {
			List<String> midCategories = mainCategoryService.getMidCategories(mainCategory);
			System.out.println("Mid categories for " + mainCategory + ": " + midCategories); // 디버깅용
			return ResponseEntity.ok(midCategories);
		} catch (Exception e) {
			System.err.println("Error getting mid categories: " + e.getMessage()); // 디버깅용
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
		}
	}

	// 상세 카테고리 조회 API - 쿼리 파라미터 방식으로 변경
	@GetMapping("/api/categories/detail")
	@ResponseBody
	public ResponseEntity<List<String>> getDetailCategories(
			@RequestParam("main") String mainCategory,
			@RequestParam("mid") String midCategory) {
		try {
			// URL 디코딩은 Spring이 자동으로 처리
			System.out.println("Main category: " + mainCategory); // 디버깅용
			System.out.println("Mid category: " + midCategory); // 디버깅용

			List<String> detailCategories = mainCategoryService.getDetailCategories(
					mainCategory,
					midCategory);

			return ResponseEntity.ok(detailCategories);
		} catch (Exception e) {
			System.err.println("Error in getDetailCategories: " + e.getMessage());
			e.printStackTrace();
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
		}
	}

	// 특정 카테고리의 도서 목록 조회
	@GetMapping("/books")
	public String getBooksByCategory(
			@RequestParam(required = false) String main,
			@RequestParam(required = false) String mid,
			@RequestParam(required = false) String detail,
			Model model) {

		// 현재 선택된 카테고리 정보 전달
		model.addAttribute("selectedMain", main);
		if (main != null) {
			model.addAttribute("midCategories", mainCategoryService.getMidCategories(main));
		}

		// 도서 목록 조회 및 전달
		List<Book> books = mainBookService.getBooksByCategory(main, mid, detail);
		model.addAttribute("books", books);

		return "books/list";
	}
}
