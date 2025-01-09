package com.bbook.service.crawling;

import java.time.Duration;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.stereotype.Component;

import io.github.bonigarcia.wdm.WebDriverManager;

@Component
public class BookTrailerCrawler {
	private WebDriver initializeDriver() {
		WebDriverManager.chromedriver().setup();
		ChromeOptions options = new ChromeOptions();
		options.addArguments("--headless");
		options.addArguments("--disable-gpu");
		options.addArguments("--no-sandbox");
		options.addArguments("--disable-dev-shm-usage");
		options.addArguments("--remote-allow-origins=*");

		return new ChromeDriver(options);
	}

	public String getBookTrailerUrl(String bookTitle) {
		WebDriver driver = null;
		try {
			driver = initializeDriver();
			System.out.println("크롤링 시작 " + bookTitle);
			WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(3));

			String kyoboUrl = "https://www.kyobobook.co.kr/";
			driver.get(kyoboUrl);

			// 검색창 대기 및 입력
			WebElement searchInput = wait.until(ExpectedConditions.elementToBeClickable(
					By.cssSelector(".ip_gnb_search")));
			searchInput.sendKeys(bookTitle);

			// 검색 버튼 클릭
			WebElement searchButton = wait.until(ExpectedConditions.elementToBeClickable(
					By.cssSelector(".btn_gnb_search")));
			searchButton.click();

			// 첫번째 검색 결과 클릭
			WebElement firstBook = wait.until(ExpectedConditions.elementToBeClickable(
					By.cssSelector("a.prod_info:first-child")));
			firstBook.click();

			try {
				WebElement trailerFrame = wait.until(ExpectedConditions.presenceOfElementLocated(
						By.cssSelector(".video_wrap iframe")));

				String trailerUrl = trailerFrame.getAttribute("src");
				System.out.println("찾은 URL : " + trailerUrl);
				return trailerUrl;
			} catch (Exception e) {
				System.out.println(e.getMessage());
				return null;
			}
		} catch (Exception e) {
			System.out.println(e.getMessage());
			return null;
		} finally {
			if (driver != null) {
				driver.quit();
			}
		}
	}
}