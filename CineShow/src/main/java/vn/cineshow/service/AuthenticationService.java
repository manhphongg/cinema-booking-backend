package vn.cineshow.service;

import vn.cineshow.dto.request.EmailRegisterRequest;
import vn.cineshow.dto.request.SignInRequest;
import vn.cineshow.dto.response.TokenResponse;

public interface AuthenticationService {
    TokenResponse getAccessToken(SignInRequest request);

    TokenResponse getRefreshToken(SignInRequest request);

}
