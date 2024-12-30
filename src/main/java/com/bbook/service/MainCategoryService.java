package com.bbook.service;

import org.springframework.stereotype.Service;
import com.bbook.repository.BookRepository;
import lombok.RequiredArgsConstructor;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MainCategoryService {

    private final BookRepository bookRepository;

    // 메인 카테고리 목록 조회
    public List<String> getMainCategories() {
        return bookRepository.findDistinctMainCategories();
    }

    // 특정 메인 카테고리의 중간 카테고리 목록 조회
    public List<String> getMidCategories(String mainCategory) {
        return bookRepository.findDistinctMidCategoriesByMainCategory(mainCategory);
    }

    // 특정 메인/중간 카테고리의 상세 카테고리 목록 조회
    public List<String> getDetailCategories(String mainCategory, String midCategory) {
        return bookRepository.findDistinctDetailCategoriesByMainAndMidCategory(mainCategory, midCategory);
    }
}