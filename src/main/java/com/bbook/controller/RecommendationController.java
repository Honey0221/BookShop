package com.bbook.controller;

import java.security.Principal;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.bbook.dto.BookRecommendationDto;
import com.bbook.service.MemberActivityService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/recommendations")
@RequiredArgsConstructor
@Slf4j
public class RecommendationController {

  private final MemberActivityService memberActivityService;

  @GetMapping("/content-based")
  public ResponseEntity<List<BookRecommendationDto>> getContentBasedRecommendations(Principal principal) {
    log.info("Content-based recommendations requested for user: {}", principal.getName());
    List<BookRecommendationDto> recommendations = memberActivityService
        .getContentBasedRecommendations(principal.getName());
    return ResponseEntity.ok(recommendations);
  }

  @GetMapping("/collaborative")
  public ResponseEntity<List<BookRecommendationDto>> getCollaborativeRecommendations(Principal principal) {
    log.info("Collaborative recommendations requested for user: {}", principal.getName());
    List<BookRecommendationDto> recommendations = memberActivityService
        .getCollaborativeRecommendations(principal.getName());
    return ResponseEntity.ok(recommendations);
  }

  @GetMapping("/hybrid")
  public ResponseEntity<List<BookRecommendationDto>> getHybridRecommendations(Principal principal) {
    log.info("Hybrid recommendations requested for user: {}", principal.getName());
    List<BookRecommendationDto> recommendations = memberActivityService.getHybridRecommendations(principal.getName());
    return ResponseEntity.ok(recommendations);
  }

  @GetMapping("/personalized")
  public ResponseEntity<List<BookRecommendationDto>> getPersonalizedRecommendations(
      Principal principal,
      @RequestParam(defaultValue = "10") int limit) {
    log.info("Personalized recommendations requested for user: {} with limit: {}",
        principal.getName(), limit);
    List<BookRecommendationDto> recommendations = memberActivityService.getHybridRecommendations(principal.getName());
    return ResponseEntity.ok(recommendations.subList(0, Math.min(limit, recommendations.size())));
  }
}