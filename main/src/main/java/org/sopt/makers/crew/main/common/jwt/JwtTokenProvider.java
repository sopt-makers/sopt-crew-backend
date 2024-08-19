package org.sopt.makers.crew.main.common.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Header;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.UnsupportedJwtException;
import jakarta.servlet.http.HttpServletRequest;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import javax.crypto.spec.SecretKeySpec;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtTokenProvider {

  @Value("${jwt.secret}")
  private String secretKey;

  @Value("${jwt.access-token.expire-length}")
  private Long accessTokenExpireLength;

  private static final String AUTHORIZATION_HEADER = "Authorization";

  public String generateAccessToken(Authentication authentication) {
    Date now = new Date();
    Date expiration = new Date(now.getTime() + accessTokenExpireLength);

    final Claims claims = Jwts.claims()
        .setIssuedAt(now)
        .setExpiration(expiration);

    claims.put("id", authentication.getPrincipal());

    return Jwts.builder()
        .setHeaderParam(Header.TYPE, Header.JWT_TYPE)
        .setClaims(claims)
        .signWith(getSignKey(), SignatureAlgorithm.HS256)
        .compact();
  }

  public Integer getAccessTokenPayload(String token) {
    return Integer.parseInt(
        Jwts.parserBuilder().setSigningKey(getSignKey()).build().parseClaimsJws(token)
            .getBody().get("id").toString());
  }

  public String resolveToken(HttpServletRequest request) {

    String header = request.getHeader(AUTHORIZATION_HEADER);

    if (header == null || !header.startsWith("Bearer ")) {
      return null;
    } else {
      return header.split(" ")[1];
    }
  }

  public JwtExceptionType validateToken(String token) {
    try {
      Jwts.parserBuilder().setSigningKey(getSignKey()).build().parseClaimsJws(token)
          .getBody();
      return JwtExceptionType.VALID_JWT_TOKEN;
    } catch (io.jsonwebtoken.security.SignatureException exception) {
      log.error("잘못된 JWT 서명을 가진 토큰입니다.");
      return JwtExceptionType.INVALID_JWT_SIGNATURE;
    } catch (MalformedJwtException exception) {
      log.error("잘못된 JWT 토큰입니다.");
      return JwtExceptionType.INVALID_JWT_TOKEN;
    } catch (ExpiredJwtException exception) {
      log.error("만료된 JWT 토큰입니다.");
      return JwtExceptionType.EXPIRED_JWT_TOKEN;
    } catch (UnsupportedJwtException exception) {
      log.error("지원하지 않는 JWT 토큰입니다.");
      return JwtExceptionType.UNSUPPORTED_JWT_TOKEN;
    } catch (IllegalArgumentException exception) {
      log.error("JWT Claims가 비어있습니다.");
      return JwtExceptionType.EMPTY_JWT;
    }
  }

  private Key getSignKey() {
    byte[] keyBytes = secretKey.getBytes(StandardCharsets.UTF_8);
    return new SecretKeySpec(keyBytes, "HmacSHA256");
  }
}
