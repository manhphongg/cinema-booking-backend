package vn.cineshow.service;


public interface OtpService {
    void sendOtp(String email, String name);             // tạo + gửi (có cooldown)
    void verifyOtp(String email, String otp); // đúng -> mark used
    boolean isEmailVerified(String email);  // dùng cho RegisterService
    void clearState(String email);          // dọn trạng thái sau khi tạo account
}