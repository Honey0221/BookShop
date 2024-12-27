package com.bbook.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import lombok.RequiredArgsConstructor;
import com.bbook.entity.Book;
import com.bbook.entity.Category;
import com.bbook.service.MainBookService;
import com.bbook.service.MainCategoryService;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.HashMap;

@Controller
@RequiredArgsConstructor
public class MainController {

	private final MainBookService mainBookService;
	private final MainCategoryService mainCategoryService;

	@GetMapping(value = "/")
	public String main(Model model) {
		// depth=1인 최상위 카테고리만 조회
		List<Category> topCategories = mainCategoryService.getTopLevelCategories()
				.stream()
				.limit(5)
				.collect(Collectors.toList());
		model.addAttribute("categories", topCategories);

		// 최상위 카테고리별로 도서 조회
		Map<Long, List<Book>> categoryBooks = mainBookService.getBooksByTopLevelCategories();
		model.addAttribute("categoryBooks", categoryBooks);

		return "main";
	}
}
