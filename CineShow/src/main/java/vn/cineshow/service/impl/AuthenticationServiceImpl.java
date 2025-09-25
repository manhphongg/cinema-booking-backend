package vn.cineshow.service.impl;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import vn.cineshow.dto.request.SignInRequest;
import vn.cineshow.dto.response.SignInResponse;
import vn.cineshow.dto.response.TokenResponse;
import vn.cineshow.model.Account;
import vn.cineshow.model.RefreshToken;
import vn.cineshow.repository.AccountRepository;
import vn.cineshow.repository.RefreshTokenRepository;
import vn.cineshow.repository.RoleRepository;
import vn.cineshow.service.AuthenticationService;
import vn.cineshow.service.JWTService;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j(topic = "AUTHENTICATION-SERVICE-IMPL")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AuthenticationServiceImpl implements AuthenticationService {

    AccountRepository accountRepository;
    JWTService jwtService;
    AuthenticationManager authenticationManager;
    RoleRepository roleRepository;
    PasswordEncoder passwordEncoder;
    EmailService emailService;
    RefreshTokenRepository refreshTokenRepository;

    @Override
    public TokenResponse signIn(SignInRequest req) {
        log.debug("getAccessToken");

        List<String> authorities = new ArrayList<>();

        try {
            Authentication authenticate = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(req.getEmail(),
                            req.getPassword()));

            log.info("isAuthenticated={}", authenticate.isAuthenticated());
            log.info("authorities={}", authenticate.getAuthorities());

            authorities.add(authenticate.getAuthorities().toString());

            SecurityContextHolder.getContext().setAuthentication(authenticate);

        } catch (AuthenticationException e) {
            log.error("login failed: {}", e.getMessage());
            throw new AuthenticationServiceException(e.getMessage());
        }

        Account account = accountRepository.getAccountByEmail(req.getEmail());

        String accessToken = jwtService.generateAccessToken(req.getEmail(), authorities);
        String refreshToken = jwtService.generateRefreshToken(req.getEmail(), authorities);

        //save refreshToken to db
        RefreshToken entity = RefreshToken.builder()
                .token(refreshToken)
                .expiryDate(Instant.now().plusSeconds(jwtService.getRefreshTokenExpiryInSecond()))
                .account(account).build();
        refreshTokenRepository.save(entity);

        log.info("accessToken={}", accessToken);
        log.info("login successful");

        return TokenResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .userId(account.getId())
                .roleName(account.getRole().getRoleName())
                .email(account.getEmail())
                .build();
    }

    @Override
    public SignInResponse refresh(String refreshToken) {
        RefreshToken entity = refreshTokenRepository.findByToken(refreshToken)
                .orElseThrow(() -> new AuthenticationServiceException("Invalid refresh token"));

        //check token out of Expiry date
        if (entity.getExpiryDate().isBefore(Instant.now())) {
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
