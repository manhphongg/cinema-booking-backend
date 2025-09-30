package vn.cineshow.service.impl;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.cineshow.dto.request.SignInRequest;
import vn.cineshow.dto.response.SignInResponse;
import vn.cineshow.dto.response.TokenResponse;
import vn.cineshow.model.Account;
import vn.cineshow.model.RefreshToken;
import vn.cineshow.repository.AccountRepository;
import vn.cineshow.repository.RefreshTokenRepository;
import vn.cineshow.service.AuthenticationService;
import vn.cineshow.service.JWTService;
import vn.cineshow.service.RefreshTokenService;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j(topic = "AUTHENTICATION-SERVICE-IMPL")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AuthenticationServiceImpl implements AuthenticationService {

    AccountRepository accountRepository;
    JWTService jwtService;
    AuthenticationManager authenticationManager;
    RefreshTokenRepository refreshTokenRepository;
    RefreshTokenService refreshTokenService;

    @Override
    @Transactional
    public TokenResponse signIn(SignInRequest req) {
        Authentication authentication;
        try {
            authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(req.getEmail(), req.getPassword())
            );
        } catch (AuthenticationException e) {
            throw new BadCredentialsException("Email or Password invalid");
        }

        SecurityContextHolder.getContext().setAuthentication(authentication);

        List<String> authorities = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .toList();

        Account account = accountRepository.findAccountByEmail(req.getEmail())
                .orElseThrow(() -> new UsernameNotFoundException("Account not found"));

        String accessToken = jwtService.generateAccessToken(req.getEmail(), authorities);
        String refreshToken = jwtService.generateRefreshToken(req.getEmail(), authorities);

        refreshTokenService.replaceRefreshToken(account, refreshToken, jwtService.getRefreshTokenExpiryInSecond());

        return TokenResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .userId(account.getId())
                .roleName(account.getRole().getRoleName())
                .email(account.getEmail())
                .build();
    }

    @Override
    @Transactional
    public SignInResponse refresh(String refreshToken) {
        RefreshToken entity = refreshTokenRepository.findByToken(refreshToken)
                .orElseThrow(() -> new AuthenticationServiceException("Invalid refresh token"));

        if (entity.getExpiryDate().isBefore(LocalDateTime.now())) {
            refreshTokenRepository.delete(entity);
            throw new AuthenticationServiceException("Refresh token expired");
        }

        Account account = entity.getAccount();
        String newAccessToken = jwtService.generateAccessToken(
                account.getEmail(),
                List.of(account.getRole().getRoleName())
        );

        return SignInResponse.builder()
                .accessToken(newAccessToken)
                .userId(account.getId())
                .roleName(account.getRole().getRoleName())
                .email(account.getEmail())
                .build();
    }


    private boolean isAccountExists(String email) {
        return accountRepository.findByEmail(email).isPresent();
    }


}