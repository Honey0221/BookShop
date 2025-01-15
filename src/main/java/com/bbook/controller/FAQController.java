package com.bbook.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import lombok.RequiredArgsConstructor;
import com.bbook.service.FAQService;
import com.bbook.constant.FAQCategory;
import com.bbook.dto.FAQDto;

import org.springframework.http.ResponseEntity;

@Controller
@RequiredArgsConstructor
public class FAQController {
  private final FAQService faqService;

  @GetMapping("/faq")
  public String faqList(Model model) {
    model.addAttribute("faqs", faqService.getAllFAQs());
    return "member/faq";
  }

  @GetMapping("/faq/{category}")
  public String faqByCategory(@PathVariable FAQCategory category, Model model) {
    model.addAttribute("faqs", faqService.getFAQsByCategory(category));
    return "member/faq";
  }

  @PostMapping("/faq")
  @ResponseBody
  public ResponseEntity<?> createFAQ(@RequestBody FAQDto faqDto) {
    faqService.saveFAQ(faqDto);
    return ResponseEntity.ok().build();
  }

  @PostMapping("/faq/{id}/update")
  @ResponseBody
  public ResponseEntity<?> updateFAQ(@PathVariable Long id, @RequestBody FAQDto faqDto) {
    faqDto.setId(id);
    faqService.updateFAQ(faqDto);
    return ResponseEntity.ok().build();
  }

  @PostMapping("/faq/{id}/delete")
  @ResponseBody
  public ResponseEntity<?> deleteFAQ(@PathVariable Long id) {
    faqService.deleteFAQ(id);
    return ResponseEntity.ok().build();
  }
}
