package vn.cineshow.service;


public interface OtpService {
    void sendOtp(String email, String name);             // tạo + gửi (có cooldown)

    void clearState(String email);// dọn trạng thái sau khi tạo account

    boolean verifyOtp(String email, String otp);
}