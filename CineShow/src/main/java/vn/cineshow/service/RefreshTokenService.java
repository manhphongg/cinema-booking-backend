package vn.cineshow.service;

import vn.cineshow.model.Account;

public interface RefreshTokenService {

    void replaceRefreshToken(Account account, String token, long expirySeconds);
}
