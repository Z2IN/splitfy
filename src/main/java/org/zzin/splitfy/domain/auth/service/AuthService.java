package org.zzin.splitfy.domain.auth.service;

import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NullMarked;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.zzin.splitfy.common.exception.BusinessException;
import org.zzin.splitfy.domain.auth.dto.request.SignupRequest;
import org.zzin.splitfy.domain.auth.dto.response.SignupResponse;
import org.zzin.splitfy.domain.auth.entity.User;
import org.zzin.splitfy.domain.auth.exception.AuthErrorCode;
import org.zzin.splitfy.domain.auth.repository.AuthRepository;

@Service
@RequiredArgsConstructor
@NullMarked
public class AuthService {

  private final AuthRepository authRepository;
  private final PasswordEncoder passwordEncoder;

  @Transactional
  public SignupResponse signup(final SignupRequest request) {

    if (authRepository.existsByEmail(request.email())) {
      throw new BusinessException(AuthErrorCode.DUPLICATE_EMAIL);
    }

    if (authRepository.existsByUsername(request.username())) {
      throw new BusinessException(AuthErrorCode.DUPLICATE_USERNAME);
    }

    String encodedPassword = passwordEncoder.encode(request.password());

    if (encodedPassword == null) {
      throw new BusinessException(AuthErrorCode.PASSWORD_ENCODING_FAILED);
    }

    User user = User.ofSignup(
        request.email(),
        request.username(),
        encodedPassword
    );

    User savedUser = authRepository.save(user);

    return new SignupResponse(
        savedUser.getId(),
        savedUser.getEmail(),
        savedUser.getUsername(),
        savedUser.getPoint()
    );
  }
}
