package org.zzin.splitfy.domain.auth.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
    @Email
    @NotBlank
    String email,
    @NotBlank
    String password
) {

}
