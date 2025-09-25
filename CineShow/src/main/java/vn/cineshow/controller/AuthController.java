package vn.cineshow.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.cineshow.dto.request.ChangePasswordRequest;
import vn.cineshow.dto.request.ForgotPasswordRequest;
import vn.cineshow.service.AuthService;

import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestBody ForgotPasswordRequest request) {
        boolean result = authService.forgotPassword(request);
        if (result) {
            return ResponseEntity.ok(Map.of("message", "OTP đã được gửi đến email của bạn"));
        }
        return ResponseEntity.badRequest().body(Map.of("error", "Email không tồn tại"));
    }

    @PutMapping("/change-password")
    public String changePassword(@RequestBody ChangePasswordRequest request) {
        boolean success = authService.changePassword(request);
        return success ? "Password changed successfully." : "Invalid credentials.";
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(
            @RequestParam String email,
            @RequestParam String otp,
            @RequestParam String newPassword) {

        boolean result = authService.resetPasswordWithOtp(email, otp, newPassword);
        if (result) {
            return ResponseEntity.ok(Map.of("message", "Mật khẩu đã được đặt lại thành công"));
        }
        return ResponseEntity.badRequest().body(Map.of("error", "OTP không hợp lệ hoặc đã hết hạn"));
    }
}
