package vn.cineshow.service.impl;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import vn.cineshow.model.Account;
import vn.cineshow.model.RefreshToken;
import vn.cineshow.repository.RefreshTokenRepository;
import vn.cineshow.service.RefreshTokenService;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class RefreshTokenServiceImpl implements RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;

    @Transactional
    public void replaceRefreshToken(Account account, String token, long expirySeconds) {
        refreshTokenRepository.deleteByAccount(account);
        RefreshToken entity = RefreshToken.builder()
                .token(token)
                .expiryDate(LocalDateTime.now().plusSeconds(expirySeconds))
                .account(account)
                .build();
        refreshTokenRepository.save(entity);
    }
}
