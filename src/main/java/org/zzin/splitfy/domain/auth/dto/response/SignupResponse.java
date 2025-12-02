package org.zzin.splitfy.domain.auth.dto.response;

public record SignupResponse(
    Long id,
    String email,
    String username,
    Long point
) {

}