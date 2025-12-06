package org.zzin.splitfy.domain.auth.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
    @Email
    @NotBlank
    String email,
    @NotBlank
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    String password
) {

}
