package com.bbook.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {
	@Value("${uploadItemPath}")
	String uploadItemPath;

	@Value("${uploadReviewPath}")
	String uploadReviewPath;

	@Override
	public void addResourceHandlers(ResourceHandlerRegistry registry) {
		// 책 이미지용 핸들러
		registry.addResourceHandler("/bookshop/book/**")
				.addResourceLocations(uploadItemPath);

		// 리뷰 이미지용 핸들러
		registry.addResourceHandler("/bookshop/review/**")
				.addResourceLocations(uploadReviewPath);
	}
}
