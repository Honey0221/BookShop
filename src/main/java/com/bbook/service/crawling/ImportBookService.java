package com.bbook.service.crawling;

import java.io.FileInputStream;
import java.io.IOException;
import java.time.LocalDateTime;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import com.bbook.constant.BookStatus;
import com.bbook.entity.Book;
import com.bbook.repository.BookRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ImportBookService {
	private final BookRepository bookRepository;

	public void importBooksFromExcel(String filePath) throws IOException {
		FileInputStream file = new FileInputStream(filePath);
		Workbook workbook = new XSSFWorkbook(file);
		Sheet sheet = workbook.getSheetAt(0);
		int totalProcessed = 0;

		for (int i = 1; i <= sheet.getLastRowNum(); i++) {
			Row row = sheet.getRow(i);
			if (row == null) continue;

			String title = getCellValue(row.getCell(1));
			String author = getCellValue(row.getCell(2));
			String publisher = getCellValue(row.getCell(3));
			String priceStr = getCellValue(row.getCell(8));
			String imageUrl = getCellValue(row.getCell(9));

			String main = getCellValue(row.getCell(4));
			String mid = getCellValue(row.getCell(5));
			String sub = getCellValue(row.getCell(6));
			String detail = getCellValue(row.getCell(7));

			String desc1 = getCellValue(row.getCell(10));
			String desc2 = getCellValue(row.getCell(11));

			if (title.isEmpty() || author.isEmpty() || publisher.isEmpty()) {
				continue;
			}

			int price = 0;
			try {
				if (!priceStr.isEmpty()) {
					price = Integer.parseInt(priceStr.replaceAll("[^0-9]", ""));
				}
			} catch (NumberFormatException e) {
				System.out.println("가격 변환 실패 : " + priceStr + " - 행 번호 : " + (i + 1));
				continue;
			}

			Book book = Book.builder()
					.title(title)
					.author(author)
					.publisher(publisher)
					.mainCategory(main)
					.midCategory(mid)
					.subCategory(sub)
					.detailCategory(detail)
					.price(price)
					.imageUrl(imageUrl)
					.description(desc1 + "\n\n" + desc2)
					.stock(100)
					.bookStatus(BookStatus.SELL)
					.createdAt(LocalDateTime.now())
					.build();

			bookRepository.save(book);
			System.out.println("도서 저장 : " + title);
			totalProcessed++;
		}
		workbook.close();
		file.close();
		System.out.println("총 " + totalProcessed + "개의 도서가 저장되었습니다.");
	}

	private String getCellValue(Cell cell) {
		if (cell == null) return "";

		return switch (cell.getCellType()) {
			case STRING -> cell.getStringCellValue().trim();
			case NUMERIC -> String.valueOf((long)cell.getNumericCellValue());
			default -> "";
		};
	}
}