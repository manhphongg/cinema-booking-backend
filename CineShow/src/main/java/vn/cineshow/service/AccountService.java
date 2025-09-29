package vn.cineshow.service;


import vn.cineshow.dto.request.EmailRegisterRequest;
import vn.cineshow.dto.request.ForgotPasswordRequest;
import vn.cineshow.dto.request.ResetPasswordRequest;

public interface AccountService {
    long createCustomerAccount(EmailRegisterRequest req);

    boolean forgotPassword(ForgotPasswordRequest request);

    boolean verifyOtpForReset(String email, String otp);

    boolean resetPassword(ResetPasswordRequest request);

}
