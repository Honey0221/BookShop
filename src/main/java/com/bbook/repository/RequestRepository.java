package com.bbook.repository;

import com.bbook.constant.RequestPriority;
import com.bbook.entity.Request;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RequestRepository extends JpaRepository<Request, Long> {
  List<Request> findByEmailOrderByCreateDateDesc(String email);

  List<Request> findAllByOrderByCreateDateDesc();

  List<Request> findByPriorityOrderByCreateDateDesc(RequestPriority priority);
}