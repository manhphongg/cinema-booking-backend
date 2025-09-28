package vn.cineshow.service;

import vn.cineshow.dto.request.EmailRegisterRequest;

public interface RegisterService {
    long registerByEmail(EmailRegisterRequest req);

    void verifyAccountAndUpdateStatus(String email, String otp);
}
