package com.bbook.service;

import org.springframework.stereotype.Service;
import com.bbook.entity.Category;
import com.bbook.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MainCategoryService {

    private final CategoryRepository categoryRepository;

    // depth=1인 최상위 카테고리만 조회
    public List<Category> getTopLevelCategories() {
        return categoryRepository.findByDepth(1);
    }

    // 특정 카테고리의 최상위 카테고리 ID 찾기
    public Long findTopLevelCategoryId(Long categoryId) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new RuntimeException("Category not found"));

        // 이미 최상위 카테고리면 그대로 반환
        if (category.getDepth() == 1) {
            return category.getId();
        }

        // parent_id를 따라 최상위 카테고리까지 올라가기
        Long currentParentId = category.getParentId();
        while (true) {
            Category parentCategory = categoryRepository.findById(currentParentId)
                    .orElseThrow(() -> new RuntimeException("Parent category not found"));

            if (parentCategory.getDepth() == 1) {
                return parentCategory.getId();
            }
            currentParentId = parentCategory.getParentId();
        }
    }
}