package org.zzin.splitfy.domain.auth.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class LoginRequest {

  @Email
  @NotBlank
  private String email;
  @NotBlank
  private String password;
}