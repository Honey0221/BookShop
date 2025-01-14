package com.bbook.service;

import com.bbook.constant.RequestPriority;
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
  private final RequestContentAnalyzerService requestContentAnalyzerService; // 내용 분석을 위한 서비스
  // 문의 생성 시 우선순위 분석 추가

  public Long createRequest(String email, String title, String content) {
    // 내용 분석하여 우선순위 결정
    RequestPriority priority = requestContentAnalyzerService.analyzePriority(content);

    Request request = Request.createRequest(email, title, content);
    request.setPriority(priority);
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

  public void updateRequestContent(Long requestId, String content) {
    Request request = requestRepository.findById(requestId)
        .orElseThrow(() -> new IllegalArgumentException("문의를 찾을 수 없습니다."));
    request.setContent(content);
    requestRepository.save(request);
  }

  // 문의 삭제
  public void deleteRequest(Long requestId) {
    requestRepository.deleteById(requestId);
  }

  // 우선순위별 문의 목록 조회
  @Transactional(readOnly = true)
  public List<RequestFormDto> getRequestsByPriority(RequestPriority priority) {
    List<Request> requests = requestRepository.findByPriorityOrderByCreateDateDesc(priority);
    return requests.stream()
        .map(RequestFormDto::of)
        .collect(Collectors.toList());
  }

}
