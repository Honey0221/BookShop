package com.bbook.service;

import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

  private final JavaMailSender mailSender;
  private final SpringTemplateEngine templateEngine;

  public void sendEmail(String to, String subject, String content) {
    try {
      MimeMessage message = mailSender.createMimeMessage();
      MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

      // Thymeleaf context 설정
      Context context = new Context();
      context.setVariable("title", subject);
      context.setVariable("content", content);

      // 템플릿을 처리하여 HTML 생성
      String htmlContent = templateEngine.process("email/emailTemplate", context);

      helper.setTo(to);
      helper.setSubject(subject);
      helper.setText(htmlContent, true); // true = HTML 형식 사용

      mailSender.send(message);
      log.info("이메일 발송 성공: {}", to);
    } catch (MessagingException e) {
      log.error("이메일 발송 실패: {}", e.getMessage());
      throw new RuntimeException("이메일 발송에 실패했습니다.", e);
    }
  }

  // 미리 정의된 템플릿 메시지 전송
  public void sendTemplateEmail(String to, String templateName, String subject, Context context) {
    try {
      MimeMessage message = mailSender.createMimeMessage();
      MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

      String htmlContent = templateEngine.process("email/" + templateName, context);

      helper.setTo(to);
      helper.setSubject(subject);
      helper.setText(htmlContent, true);

      mailSender.send(message);
      log.info("템플릿 이메일 발송 성공: {}", to);
    } catch (MessagingException e) {
      log.error("템플릿 이메일 발송 실패: {}", e.getMessage());
      throw new RuntimeException("이메일 발송에 실패했습니다.", e);
    }
  }
}
