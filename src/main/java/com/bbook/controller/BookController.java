package com.bbook.controller;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Optional;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.bbook.constant.ActivityType;
import com.bbook.entity.Book;
import com.bbook.service.BookDetailService;
import com.bbook.service.ReviewService;
import com.bbook.service.MemberService;
import com.bbook.service.MemberActivityService;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
@RequestMapping(value = "/item")
public class BookController {
	private final BookDetailService bookDetailService;
	private final ReviewService reviewService;
	private final MemberActivityService memberActivityService;
	private final MemberService memberService;

	@GetMapping
	public String getBook(@RequestParam(name = "bookId") Long id, Model model) {
		Book book = bookDetailService.getBookById(id);
		model.addAttribute("book", book);

		Double avgRating = reviewService.getAverageRatingByBookId(book.getId());
		model.addAttribute("avgRating", avgRating);

		Set<Book> authorBooks = new HashSet<>(bookDetailService
				.getBooksByAuthor(book.getAuthor()).stream()
				.filter(b -> !b.getId().equals(book.getId())).toList());

		List<Book> randomBooks = new ArrayList<>(authorBooks);
		Collections.shuffle(randomBooks);
		randomBooks = randomBooks.stream().limit(4).toList();

		model.addAttribute("authorBooks", randomBooks);

		Optional<String> memberEmail = memberService.getCurrentMemberEmail();
		if (memberEmail.isPresent()) {
			memberActivityService.saveActivity(memberEmail.get(), book.getId(), ActivityType.VIEW);
		}

		return "items/itemDtl";
	}
}
