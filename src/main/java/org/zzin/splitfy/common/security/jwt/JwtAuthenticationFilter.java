package org.zzin.splitfy.common.security.jwt;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.zzin.splitfy.common.exception.BusinessException;
import org.zzin.splitfy.common.security.AuthUser;
import org.zzin.splitfy.domain.auth.entity.User;
import org.zzin.splitfy.domain.auth.repository.AuthRepository;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

  private final JwtUtil jwtUtil;
  private final AuthRepository authRepository;

  @Override
  protected void doFilterInternal(
      HttpServletRequest req,
      HttpServletResponse res,
      FilterChain chain) throws ServletException, IOException {
    if (SecurityContextHolder.getContext().getAuthentication() != null) {
      chain.doFilter(req, res);
      return;
    }
    String token = resolveToken(req);
    if (token == null || token.isBlank()) {
      chain.doFilter(req, res);
      return;
    }

    try {
      Claims claims = jwtUtil.validateAndGetClaims(token);
      Long userId = Long.parseLong(claims.getSubject());

      User user = authRepository.findById(userId).orElse(null);
      if (user == null) {
        log.warn("사용자 없음: userId={}", userId);
        SecurityContextHolder.clearContext();
        unauthorized(res, "존재하지 않는 사용자입니다. 다시 로그인해주세요.");

        return;
      }
      AuthUser authUser = new AuthUser(userId);
      SecurityContextHolder.getContext().setAuthentication(
          new UsernamePasswordAuthenticationToken(authUser, null, Collections.emptyList())
      );

    } catch (BusinessException e) {
      log.warn("JWT 인증 실패: errorCode={}, message={}", e.getErrorCode(), e.getMessage());
      SecurityContextHolder.clearContext();
      unauthorized(res, "유효하지 않은 인증 토큰입니다. 다시 로그인해주세요.");
      return;
    }
    chain.doFilter(req, res);
  }

  private String resolveToken(HttpServletRequest req) {
    String h = req.getHeader("Authorization");

    if (h != null && h.startsWith("Bearer ")) {
      return h.substring(7);
    }
    return null;
  }

  private void unauthorized(HttpServletResponse res, String message) throws IOException {
    res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
    res.setContentType(MediaType.APPLICATION_JSON_VALUE);
    res.getWriter().write("""
        {
          "error": "UNAUTHORIZED",
          "message": "%s"
        }
        """.formatted(message));
  }
}
