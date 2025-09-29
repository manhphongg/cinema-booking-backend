package vn.cineshow.service.impl;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import vn.cineshow.enums.TokenType;
import vn.cineshow.service.JWTService;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static vn.cineshow.enums.TokenType.ACCESS_TOKEN;
import static vn.cineshow.enums.TokenType.REFRESH_TOKEN;

@Service
@Slf4j(topic = "JWT-SERVICE")
@FieldDefaults(level = AccessLevel.PRIVATE)
public class JWTServiceImpl implements JWTService {

    @Value("${jwt.secret-key-access-token}")
    String SECRET_KEY_ACCESS_TOKEN;

    @Value("${jwt.secret-key-refresh-token}")
    String SECRET_KEY_REFRESH_TOKEN;

    @Value("${jwt.expiration-access-token}")
    long expiration_access_token;

    @Value("${jwt.expiration-refresh-token}")
    long expiration_refresh_token;


    @Override
    public String generateAccessToken(String email, List<String> authorities) {
        log.info("Generate access token for  and email {}", email);
        Map<String, Object> claims = new HashMap<>();
        claims.put("roles", authorities);
        return generateToken(claims, email);
    }

    @Override
    public String generateRefreshToken(String email, List<String> authorities) {
        log.info("Generate refresh token for  and email {}", email);
        Map<String, Object> claims = new HashMap<>();
        claims.put("roles", authorities);

        return generateRefreshToken(claims, email);
    }

    @Override
    public String extractUsername(String token, TokenType tokenType) {
        log.info("Extract username from access token: {}", token);
        return extractClaims(tokenType, token, claims -> claims.getSubject());
    }

    @Override
    public long getAccessTokenExpiryInSecond() {
        return expiration_access_token / 1000;
    }

    @Override
    public long getRefreshTokenExpiryInSecond() {
        return expiration_refresh_token / 1000;
    }

    private <T> T extractClaims(TokenType type, String token, Function<Claims, T> claimsExtractor) {
        final Claims claims = extractAllClaims(type, token);
        return claimsExtractor.apply(claims);
    }

    private Claims extractAllClaims(TokenType type, String token) {
        try {
            log.info("🔍 Extracting claims from token type: {}", type);
            log.info("🔍 Token: {}...", token.substring(0, Math.min(50, token.length())));
            log.info("🔍 Secret key length: {}", getKey(type).getEncoded().length);
            
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(getKey(type))
                    .build()
                    .parseClaimsJws(token).getBody();
            
            log.info("✅ Claims extracted successfully");
            log.info("✅ Subject: {}", claims.getSubject());
            log.info("✅ Expiration: {}", claims.getExpiration());
            log.info("✅ Issued at: {}", claims.getIssuedAt());
            
            return claims;
        } catch (SignatureException e) {
            log.error("❌ JWT Signature Exception: {}", e.getMessage());
            throw new AccessDeniedException("JWT signature does not match! Token may be tampered with.");
        } catch (ExpiredJwtException e) {
            log.error("❌ JWT Expired Exception: {}", e.getMessage());
            log.error("❌ Token expired at: {}", e.getClaims().getExpiration());
            log.error("❌ Current time: {}", new Date());
            throw new AccessDeniedException("JWT token has expired! Please login again.");
        } catch (Exception e) {
            log.error("❌ JWT Processing Exception: {}", e.getMessage());
            log.error("❌ Exception type: {}", e.getClass().getSimpleName());
            e.printStackTrace();
            throw new AccessDeniedException("Invalid JWT token! Error: " + e.getMessage());
        }
    }

    private String generateToken(Map<String, Object> claims, String email) {
        log.info("Generate token for email {}", email);
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(email)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expiration_access_token))//1h
                .signWith(getKey(ACCESS_TOKEN), SignatureAlgorithm.HS256)
                .compact();
    }

    private String generateRefreshToken(Map<String, Object> claims, String email) {
        log.info("Generate token for email {}", email);
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(email)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expiration_refresh_token)) //1 day
                .signWith(getKey(REFRESH_TOKEN), SignatureAlgorithm.HS256)
                .compact();
    }

    private Key getKey(TokenType type) {
        switch (type) {
            case ACCESS_TOKEN -> {
                return Keys.hmacShaKeyFor(Decoders.BASE64.decode(SECRET_KEY_ACCESS_TOKEN));
            }
            case REFRESH_TOKEN -> {
                return Keys.hmacShaKeyFor(Decoders.BASE64.decode(SECRET_KEY_REFRESH_TOKEN));
            }
            default -> throw new IllegalArgumentException("Invalid token type");
        }
    }

}

