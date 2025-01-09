package com.bbook.controller;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Optional;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.bbook.constant.ActivityType;
import com.bbook.dto.ReviewStatsDto;
import com.bbook.entity.Book;
import com.bbook.service.BookDetailService;
import com.bbook.service.ReviewService;
import com.bbook.service.MemberService;
import com.bbook.service.MemberActivityService;
import com.bbook.service.WishBookService;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
@RequestMapping(value = "/item")
public class BookController {
	private final BookDetailService bookDetailService;
	private final ReviewService reviewService;
	private final MemberActivityService memberActivityService;
	private final MemberService memberService;
	private final WishBookService wishBookService;

	@GetMapping
	public String getBook(@RequestParam(name = "bookId") Long id, Model model) {
		try {
			Book book = bookDetailService.getBookById(id);
			model.addAttribute("book", book);

			if (book.getTrailerUrl() == null) { // 비동기 처리
				bookDetailService.getBookTrailerUrl(id);
			}

			Double avgRating = reviewService.getAverageRatingByBookId(book.getId());
			model.addAttribute("avgRating", avgRating);

			Set<Book> authorBooks = new HashSet<>(bookDetailService
					.getBooksByAuthor(book.getAuthor()).stream()
					.filter(b -> !b.getId().equals(book.getId())).toList());

			List<Book> randomBooks = new ArrayList<>(authorBooks);
			Collections.shuffle(randomBooks);
			randomBooks = randomBooks.stream().limit(4).toList();

			model.addAttribute("authorBooks", randomBooks);

			Set<Book> categoryBooks = new HashSet<>(bookDetailService
					.getBooksByMidCategory(book.getMidCategory()).stream()
					.filter(b -> !b.getId().equals(book.getId())).toList());

			List<Book> randomCategoryBooks = new ArrayList<>(categoryBooks);
			Collections.shuffle(randomCategoryBooks);
			randomCategoryBooks = randomCategoryBooks.stream().limit(4).toList();

			model.addAttribute("categoryBooks", randomCategoryBooks);

			Optional<String> memberEmail = memberService.getCurrentMemberEmail();
			if (memberEmail.isPresent()) {
				memberActivityService.saveActivity(memberEmail.get(), book.getId(),
						ActivityType.VIEW);
				bookDetailService.incrementViewCount(book.getId()); //
				Long memberId = memberService.getMemberIdByEmail(memberEmail.get());
				boolean isWished = wishBookService.isWished(memberId, book.getId());
				model.addAttribute("isWished", isWished);
			}

			ReviewStatsDto reviewStats = reviewService.getReviewStats(id);
			model.addAttribute("ratingStats", reviewStats.getRatingStats());
			model.addAttribute("avgRating", reviewStats.getAvgRating());
			model.addAttribute("tagStats", reviewStats.getTagStats());
			model.addAttribute("mostCommonTag", reviewStats.getMostCommonTag());

			return "books/bookDtl";
		} catch (Exception e) {
			System.out.println(e.getMessage());
			return null;
		}
	}

	@GetMapping("/{bookId}/trailer")
	@ResponseBody
	public Map<String, String> getBookTrailer(@PathVariable Long bookId) {
		Book book = bookDetailService.getBookById(bookId);
		return Map.of("trailerUrl",
				book.getTrailerUrl() != null ? book.getTrailerUrl() : "");
	}
}
