package com.bbook.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.bbook.entity.Category;

import java.util.List;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
  // 부모 카테고리 찾기 (ex depth = 1인 카테고리)
  List<Category> findByDepth(Integer depth);

}