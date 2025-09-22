package vn.cineshow.service.impl;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import vn.cineshow.dto.request.AccountCreationRequest;
import vn.cineshow.dto.request.SignInRequest;
import vn.cineshow.dto.response.TokenResponse;
import vn.cineshow.enums.UserRole;
import vn.cineshow.enums.UserStatus;
import vn.cineshow.exception.DuplicateResourceException;
import vn.cineshow.exception.ResourceNotFoundException;
import vn.cineshow.model.Account;
import vn.cineshow.model.Role;
import vn.cineshow.model.User;
import vn.cineshow.repository.AccountRepository;
import vn.cineshow.repository.RoleRepository;
import vn.cineshow.repository.UserRepository;
import vn.cineshow.service.AuthenticationService;
import vn.cineshow.service.JWTService;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j(topic = "AUTHENTICATION-SERVICE-IMPL")
public class AuthenticationServiceImpl implements AuthenticationService {

    private final AccountRepository accountRepository;
    private final JWTService jwtService;
    private final AuthenticationManager authenticationManager;
    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

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

    @Transactional
    @Override
    public long accountRegister(AccountCreationRequest req) {
        if (isAccountExists(req.getEmail())) {
            log.error("account already exists");
            throw new DuplicateResourceException("account already exists");
        }

        Account account = Account.builder()
                .email(req.getEmail())
                .password(passwordEncoder.encode(req.getPassword()))
                .status(UserStatus.ACTIVE)
                .build();

        Role role = roleRepository.findByRoleName((UserRole.ADMIN.name()))
                .orElseThrow(() -> new ResourceNotFoundException("role not found"));
        account.setRole(role);
        User user = User.builder()
                .name(req.getName())
                .address(req.getAddress())
                .build();
        account.setUser(user);

        accountRepository.save(account);

        return account.getId();
    }

    private boolean isAccountExists(String email) {
        return accountRepository.findByEmail(email).isPresent();
    }
}
