package com.bbook.repository;

import com.bbook.entity.FAQEntity;
import com.bbook.constant.FAQCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FAQRepository extends JpaRepository<FAQEntity, Long> {
  List<FAQEntity> findAllByOrderByIdDesc();

  List<FAQEntity> findByCategoryOrderByIdDesc(FAQCategory category);
}
