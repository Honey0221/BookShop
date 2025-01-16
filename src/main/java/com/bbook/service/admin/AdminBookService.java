package com.bbook.service.admin;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import com.bbook.constant.BookStatus;
import com.bbook.dto.BookFormDto;
import com.bbook.dto.CategoryCountDto;
import com.bbook.dto.CategoryViewCountDto;
import com.bbook.entity.Book;
import com.bbook.repository.AdminBookRepository;
import com.bbook.service.FileService;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class AdminBookService {
	private final AdminBookRepository adminBookRepository;
	private final FileService fileService;
	private final TelegramAlertService telegramAlertService;

	@Value("${itemImgLocation}")
	private String itemImgLocation;

	public Page<Book> getAdminBookPage(Pageable pageable) {
		return adminBookRepository.findAll(pageable);
	}

	public Page<Book> getFilteredBooks(String searchType, String keyword,
			String status, PageRequest pageable) {
		if (StringUtils.hasText(keyword)) {
			switch (searchType) {
				case "title":
					return adminBookRepository.findByTitleContainingAndBookStatus(
							keyword, getBookStatus(status), pageable);
				case "author":
					return adminBookRepository.findByAuthorContainingAndBookStatus(
							keyword, getBookStatus(status), pageable);
				case "publisher":
					return adminBookRepository.findByPublisherContainingAndBookStatus(
							keyword, getBookStatus(status), pageable);
			}
		}

		if (StringUtils.hasText(status)) {
			return adminBookRepository.findByBookStatus(BookStatus.valueOf(status), pageable);
		}

		return adminBookRepository.findAll(pageable);
	}

	private BookStatus getBookStatus(String status) {
		return StringUtils.hasText(status) ? BookStatus.valueOf(status) : BookStatus.SELL;
	}

	@Transactional(readOnly = true)
	public ByteArrayResource generateExcel(String searchType, String keyword,
			String status, String sort) throws IOException {
		// 검색 조건에 맞는 모든 도서 조회
		List<Book> books = getFilteredBooksForExcel(searchType, keyword, status, sort);

		try(Workbook workbook = new XSSFWorkbook()) {
			Sheet sheet = workbook.createSheet("책 목록");

			Row headerRow = sheet.createRow(0);
			String[] headers = {"ID", "제목", "저자", "출판사", "재고", "가격", "상태", "등록일"};
			for (int i = 0; i < headers.length; i++) {
				Cell cell = headerRow.createCell(i);
				cell.setCellValue(headers[i]);
			}

			int rowNum = 1;
			for (Book book : books) {
				Row row = sheet.createRow(rowNum++);
				row.createCell(0).setCellValue(book.getId());
				row.createCell(1).setCellValue(book.getTitle());
				row.createCell(2).setCellValue(book.getAuthor());
				row.createCell(3).setCellValue(book.getPublisher());
				row.createCell(4).setCellValue(book.getStock());
				row.createCell(5).setCellValue(book.getPrice());
				row.createCell(6).setCellValue(book.getBookStatus().name());
				row.createCell(7).setCellValue(book.getCreatedAt().format(
						DateTimeFormatter.ofPattern("yyyy-MM-dd")
				));
			}

			for (int i = 0; i < headers.length; i++) {
				sheet.autoSizeColumn(i);
			}

			// 엑셀 파일을 바이트 배열로 변환
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			workbook.write(bos);
			byte[] bytes = bos.toByteArray();

			return new ByteArrayResource(bytes);
		}
	}

	private List<Book> getFilteredBooksForExcel(
			String searchType, String keyword, String status, String sort) {
		// 정렬 정보 파싱
		String[] sortParts = sort.split(",");
		Sort sorting = Sort.by(sortParts[1].equals("asc") ?
				Sort.Direction.ASC : Sort.Direction.DESC, sortParts[0]);

		// 검색 조건에 따른 도서 목록 조회
		if (StringUtils.hasText(keyword)) {
			switch (searchType) {
				case "title":
					return adminBookRepository.findByTitleContainingAndBookStatus(
							keyword, getBookStatus(status), sorting);
				case "author":
					return adminBookRepository.findByAuthorContainingAndBookStatus(
							keyword, getBookStatus(status), sorting);
				case "publisher":
					return adminBookRepository.findByPublisherContainingAndBookStatus(
							keyword, getBookStatus(status), sorting);
			}
		}

		if (StringUtils.hasText(status)) {
			return adminBookRepository.findByBookStatus(BookStatus.valueOf(status), sorting);
		}
		return adminBookRepository.findAll(sorting);
	}

	public Book saveBook(BookFormDto bookFormDto, MultipartFile bookImage)
			throws Exception {
		String savedFileName = "";
		if (bookImage != null && !bookImage.isEmpty()) {
			savedFileName = fileService.uploadFile(itemImgLocation,
											bookImage.getOriginalFilename(),
											bookImage.getBytes());
		}

		Book book = Book.builder()
				.title(bookFormDto.getTitle())
				.author(bookFormDto.getAuthor())
				.publisher(bookFormDto.getPublisher())
				.price(bookFormDto.getPrice())
				.stock(bookFormDto.getStock())
				.mainCategory(bookFormDto.getMainCategory())
				.midCategory(bookFormDto.getMidCategory())
				.subCategory(bookFormDto.getSubCategory())
				.detailCategory(bookFormDto.getDetailCategory())
				.description(bookFormDto.getDescription())
				.imageUrl(savedFileName)
				.bookStatus(BookStatus.SELL)
				.createdAt(LocalDateTime.now())
				.viewCount(0L)
				.build();

		Book savedBook = adminBookRepository.save(book);

		// 도서 저장 후 알림 발송
		telegramAlertService.sendNewBookAlert(bookFormDto);

		return savedBook;
	}

	public BookFormDto getBookId(Long bookId) {
		Book book = adminBookRepository.findById(bookId)
				.orElseThrow(() -> new EntityNotFoundException("도서를 찾을 수 없습니다."));
		return BookFormDto.builder()
				.title(book.getTitle())
				.author(book.getAuthor())
				.publisher(book.getPublisher())
				.price(book.getPrice())
				.stock(book.getStock())
				.mainCategory(book.getMainCategory())
				.midCategory(book.getMidCategory())
				.subCategory(book.getSubCategory())
				.detailCategory(book.getDetailCategory())
				.description(book.getDescription())
				.imageUrl(book.getImageUrl())
				.build();
	}

	public void updateBook(Long bookId, BookFormDto bookFormDto,
			MultipartFile bookImage) throws Exception {
		Book book = adminBookRepository.findById(bookId)
				.orElseThrow(() -> new EntityNotFoundException("도서를 찾을 수 없습니다."));

		if (bookImage != null && !bookImage.isEmpty()) {
			if (book.getImageUrl() != null) {
				fileService.deleteFile(itemImgLocation, book.getImageUrl());
			}

			String savedFileName = fileService.uploadFile(itemImgLocation,
															bookImage.getOriginalFilename(),
															bookImage.getBytes());

			System.out.println("이미지 저장 완료 : " + savedFileName);
			book.setImageUrl(savedFileName);
		}

		book.updateBook(bookFormDto);
		adminBookRepository.save(book);
	}

	public void deleteBook(Long bookId) throws Exception {
		Book book = adminBookRepository.findById(bookId)
				.orElseThrow(() -> new EntityNotFoundException("도서를 찾을 수 없습니다."));

		if (book.getImageUrl() != null) {
			fileService.deleteFile(itemImgLocation, book.getImageUrl());
		}

		adminBookRepository.delete(book);
	}

	public Map<String, Long> getBookStatusDistribution() {
		Map<String, Long> distribution = new HashMap<>();
		distribution
				.put("selling", adminBookRepository.countByBookStatus(BookStatus.SELL));
		distribution
				.put("soldOut", adminBookRepository.countByBookStatus(BookStatus.SOLD_OUT));

		return distribution;
	}

	public Map<String, Object> getCategoryDistribution() {
		List<CategoryCountDto> allCategories = adminBookRepository.countByMidCategory();

		// 상위 10개 카테고리 선택
		List<CategoryCountDto> topCategories = allCategories.stream()
				.limit(10).toList();

		// 나머지 카테고리 기타로 병합
		long othersCount = allCategories.stream()
				.skip(10).mapToLong(CategoryCountDto::getCount).sum();

		Map<String, Object> result = new HashMap<>();

		List<String> labels = new ArrayList<>(topCategories.stream()
				.map(CategoryCountDto::getCategory).toList());
		List<Long> data = new ArrayList<>(topCategories.stream()
				.map(CategoryCountDto::getCount).toList());

		// 기타 카테고리 추가
		if (othersCount > 0) {
			labels.add("기타");
			data.add(othersCount);
		}

		result.put("labels", labels);
		result.put("data", data);

		return result;
	}

	public Map<String, Object> getPriceRangeStats() {
		List<Object[]> priceRangeStats = adminBookRepository.findBooksByPriceRange();

		Map<String, Object> result = new HashMap<>();

		List<String> labels = new ArrayList<>();
		List<Long> data = new ArrayList<>();

		for (Object[] stat : priceRangeStats) {
			labels.add((String) stat[0]);
			data.add((Long) stat[1]);
		}

		result.put("labels", labels);
		result.put("data", data);

		return result;
	}

	public Map<String, Object> getLowStockBooks() {
		List<Book> lowStockBooks = adminBookRepository.findTop5ByOrderByStockAsc();

		Map<String, Object> result = new HashMap<>();
		result.put("labels", lowStockBooks.stream()
				.map(Book::getTitle).toList());
		result.put("data", lowStockBooks.stream()
				.map(Book::getStock).toList());

		return result;
	}

	public Map<String, Object> getTopViewedBooks() {
		List<Book> topBooks = adminBookRepository.findTop3ByOrderByViewCountDesc();

		Map<String, Object> result = new HashMap<>();
		result.put("labels", topBooks.stream()
				.map(Book::getTitle).toList());
		result.put("data", topBooks.stream()
				.map(Book::getViewCount).toList());

		return result;
	}

	public Map<String, Object> getTopViewedCategories() {
		PageRequest pageRequest = PageRequest.of(0, 3);
		List<CategoryViewCountDto> topCategories
				= adminBookRepository.findFirst3MidCategoriesByViewCount(pageRequest);

		Map<String, Object> result = new HashMap<>();
		result.put("labels", topCategories.stream()
				.map(CategoryViewCountDto::getCategory).toList());
		result.put("data", topCategories.stream()
				.map(CategoryViewCountDto::getViewCount).toList());

		return result;
	}

}
