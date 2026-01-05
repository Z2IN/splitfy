package org.zzin.splitfy.common.security.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import javax.crypto.SecretKey;
import org.springframework.stereotype.Component;
import org.zzin.splitfy.common.exception.BusinessException;
import org.zzin.splitfy.common.security.AuthUser;
import org.zzin.splitfy.domain.auth.exception.AuthErrorCode;

@Component
public class JwtUtil {

  private final JwtProperties jwtProperties;
  private final Duration accessTokenExpiration;
  private final SecretKey key;
  private final JwtParser parser;

  public JwtUtil(JwtProperties jwtProperties) {
    this.jwtProperties = jwtProperties;
    this.accessTokenExpiration = jwtProperties.accessTokenExpiration();

    this.key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtProperties.secret()));

    this.parser = Jwts.parser()
        .clockSkewSeconds(60)
        .verifyWith(key)
        .requireIssuer(jwtProperties.issuer())
        .build();
  }

  public String createAccessToken(AuthUser user) {
    Instant now = Instant.now();

    return Jwts.builder().
        subject(String.valueOf(user.userId()))
        .issuer(jwtProperties.issuer())
        .issuedAt(Date.from(now))
        .expiration(Date.from(now.plus(accessTokenExpiration)))
        .signWith(key)
        .compact();
  }

  public Claims validateAndGetClaims(String token) {
    try {
      return parser.parseSignedClaims(token).getPayload();
    } catch (ExpiredJwtException e) {
      throw new BusinessException(AuthErrorCode.TOKEN_EXPIRED);
    } catch (JwtException e) {
      throw new BusinessException(AuthErrorCode.INVALID_TOKEN);
    }
  }
}
