package com.bbook.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MemberNicknameDto {

  @NotEmpty(message = "닉네임은 필수 입력값입니다.")
  @Size(min = 2, max = 10, message = "닉네임은 2자 이상 10자 이하로 입력해주세요.")
  private String nickname;
}