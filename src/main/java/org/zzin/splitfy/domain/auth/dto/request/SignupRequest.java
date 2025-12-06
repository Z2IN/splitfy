package org.zzin.splitfy.domain.auth.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class SignupRequest {

  @Email(message = "올바른 이메일 형식이 아닙니다.")
  @NotBlank(message = "이메일은 필수 입력값입니다.")
  String email;

  @NotBlank(message = "비밀번호는 필수 입력값입니다.")
  @Size(min = 8, max = 20, message = "비밀번호는 20자 이내여야 합니다.")
  String password;

  @NotBlank(message = "사용자 이름은 필수 입력값입니다.")
  String username;
}