package vn.cineshow.service;

import vn.cineshow.dto.request.AccountCreationRequest;
import vn.cineshow.dto.request.SignInRequest;
import vn.cineshow.dto.response.TokenResponse;

public interface AuthenticationService {
    TokenResponse getAccessToken(SignInRequest request);

    TokenResponse getRefreshToken(SignInRequest request);

    long accountRegister(AccountCreationRequest req);
}
