package vn.cineshow.service;

import vn.cineshow.dto.request.AccountCreationRequest;
import vn.cineshow.dto.request.ChangePasswordRequest;
import vn.cineshow.dto.request.SignInRequest;
import vn.cineshow.dto.response.SignInResponse;
import vn.cineshow.dto.response.TokenResponse;

public interface AuthenticationService {

    TokenResponse signIn(SignInRequest req);

    SignInResponse refresh(String refreshToken);

    long accountRegister(AccountCreationRequest req);

    void changePassword(ChangePasswordRequest req);

    void forgotPassword(String email);


}
