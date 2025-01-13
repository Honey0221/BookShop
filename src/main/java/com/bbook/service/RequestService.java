package com.bbook.service;

import com.bbook.constant.RequestStatus;
import com.bbook.dto.RequestFormDto;
import com.bbook.entity.Request;
import com.bbook.repository.RequestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class RequestService {

  private final RequestRepository requestRepository;

  // 문의 생성
  public Long createRequest(String email, String title, String content) {
    Request request = Request.createRequest(email, title, content);
    requestRepository.save(request);
    return request.getId();
  }

  // 문의 목록 조회 (이메일별)
  @Transactional(readOnly = true)
  public List<RequestFormDto> getRequestsByEmail(String email) {
    List<Request> requests = requestRepository.findByEmailOrderByCreateDateDesc(email);
    return requests.stream()
        .map(RequestFormDto::of)
        .collect(Collectors.toList());
  }

  // 문의 상세 조회
  @Transactional(readOnly = true)
  public RequestFormDto getRequest(Long requestId) {
    Request request = requestRepository.findById(requestId)
        .orElseThrow(() -> new IllegalArgumentException("문의를 찾을 수 없습니다."));
    return RequestFormDto.of(request);
  }

  // 답변 등록
  public void addAnswer(Long requestId, String answer) {
    Request request = requestRepository.findById(requestId)
        .orElseThrow(() -> new IllegalArgumentException("문의를 찾을 수 없습니다."));
    request.addAnswer(answer);
    request.setStatus(RequestStatus.ANSWERED);
    requestRepository.save(request);
  }

  public void updateRequestStatus(Long requestId, RequestStatus status) {
    Request request = requestRepository.findById(requestId)
        .orElseThrow(() -> new IllegalArgumentException("문의를 찾을 수 없습니다."));
    request.setStatus(status);
    requestRepository.save(request);
  }

  @Transactional(readOnly = true)
  public List<RequestFormDto> getAllRequests() {
    List<Request> requests = requestRepository.findAllByOrderByCreateDateDesc();
    return requests.stream()
        .map(RequestFormDto::of)
        .collect(Collectors.toList());
  }
}
