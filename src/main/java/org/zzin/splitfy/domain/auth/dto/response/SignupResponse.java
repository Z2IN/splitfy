package org.zzin.splitfy.domain.auth.dto.response;

public record SignupResponse(
    long id,
    String email,
    String username,
    long point
) {

}