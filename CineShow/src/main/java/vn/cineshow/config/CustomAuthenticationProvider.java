package vn.cineshow.config;

import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import vn.cineshow.enums.AccountStatus;
import vn.cineshow.exception.AppException;
import vn.cineshow.exception.ErrorCode;
import vn.cineshow.model.Account;
import vn.cineshow.service.impl.AccountDetailsService;

@Component
@RequiredArgsConstructor
public class CustomAuthenticationProvider implements AuthenticationProvider {

    private final AccountDetailsService accountDetailsService;
    private final PasswordEncoder passwordEncoder;

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        String email = authentication.getName();
        String rawPassword = authentication.getCredentials().toString();

        Account account = (Account) accountDetailsService.loadUserByUsername(email);

        if (!passwordEncoder.matches(rawPassword, account.getPassword())) {
            throw new BadCredentialsException("Invalid email or password");
        }

        if (account.getStatus() == AccountStatus.PENDING) {
            throw new AppException(ErrorCode.EMAIL_UN_VERIFIED);
        }
        if (account.getStatus() == AccountStatus.DEACTIVATED || account.isDeleted()) {
            throw new AppException(ErrorCode.ACCOUNT_INACTIVE);
        }

        return new UsernamePasswordAuthenticationToken(account, null, account.getAuthorities());
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication);
    }
}
