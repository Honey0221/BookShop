package com.bbook.controller.admin;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.bbook.dto.BookFormDto;
import com.bbook.entity.Book;
import com.bbook.repository.AdminBookRepository;
import com.bbook.service.admin.AdminBookService;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
@RequestMapping("/admin")
public class AdminBookController {
	private final AdminBookService adminBookService;
	private final AdminBookRepository adminBookRepository;

	@GetMapping("/bookMng")
	public String itemManage(@PageableDefault(size = 20, sort = "id",
			direction = Sort.Direction.DESC) Pageable pageable, Model model) {
		Page<Book> books = adminBookService.getAdminBookPage(pageable);
		model.addAttribute("books", books);

		return "/admin/books/bookMng";
	}

	@GetMapping("/items/list")
	@ResponseBody
	public Page<Book> getBookList(
			@RequestParam(required = false) String searchType,
			@RequestParam(required = false) String keyword,
			@RequestParam(required = false) String status,
			@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "10") int size,
			@RequestParam(defaultValue = "id,desc") String sort) {
		return adminBookService.getFilteredBooks(searchType, keyword, status,
				PageRequest.of(page, size, getSortFromString(sort)));
	}


	@GetMapping("/items/excel-download")
	public ResponseEntity<Resource> downloadExcel(
			@RequestParam(required = false) String searchType,
			@RequestParam(required = false) String keyword,
			@RequestParam(required = false) String status,
			@RequestParam(defaultValue = "id,desc") String sort) {
		try {
			// 엑셀 파일 생성
			ByteArrayResource resource
					= adminBookService.generateExcel(searchType, keyword, status, sort);
			// 파일명 정의
			String fileName = "BooksList_" + LocalDateTime.now().format(
					DateTimeFormatter.ofPattern("yyyyMMdd")) + ".xlsx";

			String encodedFileName = URLEncoder.encode(fileName, StandardCharsets.UTF_8)
					.replaceAll("\\+", "%20");

			return ResponseEntity.ok()
					.contentType(MediaType.parseMediaType(
							"application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
					.contentLength(resource.contentLength())
					.header(HttpHeaders.CONTENT_DISPOSITION,
							"attachment; fileName*=UTF-8''" + encodedFileName)
					.header(HttpHeaders.CACHE_CONTROL,
							"no-cache, no-store, must-revalidate")
					.header(HttpHeaders.PRAGMA, "no-cache")
					.header(HttpHeaders.EXPIRES, "0")
					.body(resource);
		} catch (Exception e) {
			System.out.println("엑셀 파일 생성 중 오류 발생" + e.getMessage());
			return ResponseEntity.internalServerError().build();
		}
	}

	private Sort getSortFromString(String sort) {
		String[] parts = sort.split(",");
		return Sort.by(parts[1].equals("desc") ?
				Sort.Direction.DESC : Sort.Direction.ASC, parts[0]);
	}

	@PostMapping("/items/new")
	@ResponseBody
	public ResponseEntity<Book> createBook(
			@RequestPart(value = "bookFormDto") BookFormDto bookFormDto,
			@RequestPart(value = "bookImage") MultipartFile bookImage) {
		try {
			System.out.println("받은 데이터 : " + bookFormDto);
			Book savedBook = adminBookService.saveBook(bookFormDto, bookImage);
			return ResponseEntity.ok(savedBook);
		} catch (Exception e) {
			System.out.println("에러 발생 " + e.getMessage());
			return ResponseEntity.badRequest().build();
		}
	}

	@GetMapping("/items/{bookId}")
	@ResponseBody
	public ResponseEntity<BookFormDto> getBook(@PathVariable Long bookId) {
		try {
			BookFormDto bookFormDto = adminBookService.getBookId(bookId);
			return ResponseEntity.ok(bookFormDto);
		} catch (EntityNotFoundException e) {
			return ResponseEntity.notFound().build();
		}
	}

	@PutMapping("/items/{bookId}")
	@ResponseBody
	public ResponseEntity<Void> updateBook(
			@PathVariable Long bookId,
			@RequestPart(value = "bookFormDto") BookFormDto bookFormDto,
			@RequestPart(value = "bookImage", required = false) MultipartFile bookImage) {
		try {
			adminBookService.updateBook(bookId, bookFormDto, bookImage);
			return ResponseEntity.ok().build();
		} catch (EntityNotFoundException e) {
			return ResponseEntity.notFound().build();
		} catch (Exception e) {
			return ResponseEntity.internalServerError().build();
		}
	}

	@DeleteMapping("/items/{bookId}")
	@ResponseBody
	public ResponseEntity<Void> deleteBook(@PathVariable Long bookId) {
		try {
			adminBookService.deleteBook(bookId);
			return ResponseEntity.ok().build();
		} catch (EntityNotFoundException e) {
			return ResponseEntity.notFound().build();
		} catch (Exception e) {
			return ResponseEntity.internalServerError().build();
		}
	}

	@GetMapping("/categories/main")
	@ResponseBody
	public List<String> getMainCategories() {
		return adminBookRepository.findDistinctMainCategories();
	}

	@GetMapping("/categories/mid")
	@ResponseBody
	public List<String> getMidCategories() {
		return adminBookRepository.findDistinctMidCategories();
	}

	@GetMapping("/categories/sub")
	@ResponseBody
	public List<String> getSubCategories() {
		return adminBookRepository.findDistinctSubCategories();
	}

	@GetMapping("/categories/detail")
	@ResponseBody
	public List<String> getDetailCategories() {
		return adminBookRepository.findDistinctDetailCategories();
	}

	@GetMapping("/bookStat")
	public String itemStatistic() {
		return "/admin/books/bookStat";
	}

	@GetMapping("/stats/status")
	@ResponseBody
	public Map<String, Long> getBookStatusStats() {
		return adminBookService.getBookStatusDistribution();
	}

	@GetMapping("/stats/category")
	@ResponseBody
	public Map<String, Object> getCategoryStats() {
		return adminBookService.getCategoryDistribution();
	}

	@GetMapping("/stats/price-range")
	@ResponseBody
	public Map<String, Object> getPriceRangeStats() {
		return adminBookService.getPriceRangeStats();
	}

	@GetMapping("/stats/low-stock")
	@ResponseBody
	public Map<String, Object> getLowStockStats() {
		return adminBookService.getLowStockBooks();
	}

	@GetMapping("/stats/top-viewed-books")
	@ResponseBody
	public Map<String, Object> getTopViewedBooks() {
		return adminBookService.getTopViewedBooks();
	}

	@GetMapping("/stats/top-viewed-categories")
	@ResponseBody
	public Map<String, Object> getTopViewedCategories() {
		return adminBookService.getTopViewedCategories();
	}
}
