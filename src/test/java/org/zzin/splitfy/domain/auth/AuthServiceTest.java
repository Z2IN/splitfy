package org.zzin.splitfy.domain.auth;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;
import org.zzin.splitfy.common.exception.BusinessException;
import org.zzin.splitfy.domain.auth.dto.request.SignupRequest;
import org.zzin.splitfy.domain.auth.dto.response.SignupResponse;
import org.zzin.splitfy.domain.auth.entity.User;
import org.zzin.splitfy.domain.auth.exception.AuthErrorCode;
import org.zzin.splitfy.domain.auth.repository.AuthRepository;
import org.zzin.splitfy.domain.auth.service.AuthService;

@ExtendWith(MockitoExtension.class)
public class AuthServiceTest {

  @Mock
  private AuthRepository authRepository;

  @Mock
  private PasswordEncoder passwordEncoder;

  @InjectMocks
  private AuthService authService;
  private AuthErrorCode UserErrorCode;

  private SignupRequest createRequest() {
    SignupRequest req = new SignupRequest();
    ReflectionTestUtils.setField(req, "email", "test@example.com");
    ReflectionTestUtils.setField(req, "password", "password");
    ReflectionTestUtils.setField(req, "username", "username");
    return req;
  }

  @Test
  void signup_유효한_정보로_회원가입에_성공() {
    SignupRequest request = createRequest();

    given(authRepository.existsByEmail(request.getEmail())).willReturn(false);
    given(authRepository.existsByUsername(request.getUsername())).willReturn(false);
    given(passwordEncoder.encode(request.getPassword())).willReturn("encodedPw");

    User savedUser = User.ofSignup(request.getEmail(), request.getUsername(), "encodedPw");
    ReflectionTestUtils.setField(savedUser, "id", 1L);
    given(authRepository.save(any(User.class))).willReturn(savedUser);

    SignupResponse response = authService.signup(request);

    then(authRepository).should(times(1)).save(any(User.class));
    assertThat(response.email()).isEqualTo(request.getEmail());
    assertThat(response.username()).isEqualTo(request.getUsername());
    assertThat(response.point()).isEqualTo(0L);
  }

  @Test
  void signup_이메일중복_예외발생() {
    SignupRequest request = createRequest();
    given(authRepository.existsByEmail(request.getEmail())).willReturn(true);

    assertThatThrownBy(() -> authService.signup(request))
        .isInstanceOf(BusinessException.class)
        .hasMessage(UserErrorCode.DUPLICATE_EMAIL.getMessage());

    then(authRepository).should(never()).save(any(User.class));
  }

  @Test
  void signup_닉네임중복_예외발생() {
    SignupRequest request = createRequest();
    given(authRepository.existsByEmail(request.getEmail())).willReturn(false);
    given(authRepository.existsByUsername(request.getUsername())).willReturn(true);

    assertThatThrownBy(() -> authService.signup(request))
        .isInstanceOf(BusinessException.class)
        .hasMessage(UserErrorCode.DUPLICATE_USERNAME.getMessage());

    then(authRepository).should(never()).save(any(User.class));
  }
}
