package vn.cineshow.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import vn.cineshow.dto.request.SignInRequest;
import vn.cineshow.dto.response.TokenResponse;
import vn.cineshow.repository.UserRepository;
import vn.cineshow.service.AuthenticationService;
import vn.cineshow.service.JWTService;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j(topic = "AUTHENTICATION-SERVICE-IMPL")
public class AuthenticationServiceImpl implements AuthenticationService {

    UserRepository userRepository;
    JWTService jwtService;
    AuthenticationManager authenticationManager;

    @Override
    public TokenResponse getAccessToken(SignInRequest request) {
        log.debug("getAccessToken");

        List<String> authorities = new ArrayList<>();

        try {
            Authentication authenticate = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(),
                            request.getPassword()));

            log.info("isAuthenticated={}", authenticate.isAuthenticated());
            log.info("authorities={}", authenticate.getAuthorities());

            authorities.add(authenticate.getAuthorities().toString());

            SecurityContextHolder.getContext().setAuthentication(authenticate);

        } catch (AuthenticationException e) {
            log.error("login failed: {}", e.getMessage());
            throw new AuthenticationServiceException(e.getMessage());
        }

        String accessToken = jwtService.generateAccessToken(request.getEmail(), authorities);
        String refreshToken = jwtService.generateRefreshToken(request.getEmail(), authorities);
        log.info("accessToken={}", accessToken);
        log.info("login successful");

        return TokenResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }

    @Override
    public TokenResponse getRefreshToken(SignInRequest request) {
        return null;
    }
}
