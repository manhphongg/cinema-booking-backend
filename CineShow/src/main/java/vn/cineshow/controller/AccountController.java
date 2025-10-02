package vn.cineshow.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import vn.cineshow.dto.request.EmailRegisterRequest;
import vn.cineshow.dto.request.ForgotPasswordRequest;
import vn.cineshow.dto.request.OtpVerifyDTO;
import vn.cineshow.dto.request.ResetPasswordRequest;
import vn.cineshow.dto.response.ResponseData;
import vn.cineshow.exception.IllegalParameterException;
import vn.cineshow.service.AccountService;
import vn.cineshow.service.RegisterService;
import vn.cineshow.dto.request.ChangePasswordRequest;

import java.util.UUID;
@Slf4j
@RestController
@RequestMapping("/accounts")
@RequiredArgsConstructor
public class AccountController {
    private final RegisterService registerService;
    private final AccountService accountService;

    // B1: Nhận info, lưu Redis, gửi OTP
    @PostMapping("/register-email")
    public ResponseData<?> registerEmail(@RequestBody @Valid EmailRegisterRequest req) {
        if (!req.password().equals(req.confirmPassword())) {
            throw new IllegalParameterException("password != confirmPassword");
        }

        long id = registerService.registerByEmail(req);
        return new ResponseData<>(HttpStatus.CREATED.value(),
                "Temp account created. Please verify OTP to activate.", id);
    }

    // B2: Verify OTP
    // ====== VERIFY OTP (B2) - CÓ LOG CHI TIẾT ======
    @PostMapping("/verify-otp")
    public ResponseData<?> verifyOtp(@RequestParam String email, @RequestParam String otp) {
        final long start = System.currentTimeMillis();
        final String safeEmail = maskEmail(email);

        // KHÔNG log OTP để tránh lộ thông tin nhạy cảm
        log.info("verify-otp start email={}", safeEmail);

        try {
            registerService.verifyAccountAndUpdateStatus(email, otp);

            long elapsed = System.currentTimeMillis() - start;
            log.info("verify-otp success email={} elapsedMs={}", safeEmail, elapsed);

            return new ResponseData<>(HttpStatus.OK.value(), "Account activated successfully", null);

            // Các lỗi “dự đoán được” -> log WARN, trả 400
        } catch (IllegalParameterException e) {
            long elapsed = System.currentTimeMillis() - start;
            log.warn("verify-otp invalid-parameter email={} elapsedMs={} reason={}",
                    safeEmail, elapsed, e.getMessage());
            return new ResponseData<>(HttpStatus.BAD_REQUEST.value(), e.getMessage(), null);

            // Nếu có custom exception từ service, ví dụ:
            // InvalidOtpException, OtpExpiredException, AccountNotFoundException...
        } catch (RuntimeException e) {
            // Có thể thay bằng catch (InvalidOtpException | OtpExpiredException | AccountNotFoundException e)
            // nếu bạn đã định nghĩa rõ các exception này.
            long elapsed = System.currentTimeMillis() - start;
            log.warn("verify-otp business-failure email={} elapsedMs={} reason={}",
                    safeEmail, elapsed, e.getMessage());
            return new ResponseData<>(HttpStatus.BAD_REQUEST.value(), "Failed to verify OTP: " + e.getMessage(), null);

            // Lỗi không lường trước -> log ERROR kèm traceId, trả 500
        } catch (Exception e) {
            String traceId = UUID.randomUUID().toString();
            long elapsed = System.currentTimeMillis() - start;

            log.error("verify-otp error email={} traceId={} elapsedMs={}",
                    safeEmail, traceId, elapsed, e);

            return new ResponseData<>(HttpStatus.INTERNAL_SERVER_ERROR.value(),
                    "Failed to verify OTP (traceId=" + traceId + ")", null);
        }
    }


    // Quên mật khẩu → gửi OTP
    @PostMapping("/forgot-password")
    public ResponseData<?> forgotPassword(@RequestBody @Valid ForgotPasswordRequest request) {
        boolean sent = accountService.forgotPassword(request);
        if (!sent) {
            return new ResponseData<>(HttpStatus.BAD_REQUEST.value(),
                    "Email not found or OTP could not be sent", null);
        }
        return new ResponseData<>(HttpStatus.OK.value(),
                "OTP sent to your email", null);
    }

    // B3: Verify OTP khi reset password
    @PostMapping("/verify-otp-reset")
    public ResponseData<?> verifyOtpReset(@RequestBody @Valid OtpVerifyDTO req) {
        boolean valid = accountService.verifyOtpForReset(req.email(), req.otpCode());
        if (!valid) {
            return new ResponseData<>(HttpStatus.BAD_REQUEST.value(),
                    "Invalid or expired OTP", null);
        }
        return new ResponseData<>(HttpStatus.OK.value(),
                "OTP verified. You can now reset your password.", null);
    }

    // B4: Đặt lại mật khẩu bằng OTP
    @PostMapping("/reset-password")
    public ResponseData<?> resetPassword(@RequestBody @Valid ResetPasswordRequest request) {
        boolean success = accountService.resetPassword(request);
        if (!success) {
            return new ResponseData<>(HttpStatus.BAD_REQUEST.value(),
                    "Invalid OTP or expired", null);
        }
        return new ResponseData<>(HttpStatus.OK.value(),
                "Password reset successfully", null);
    }

    @PostMapping("/change-password")
    public ResponseData<?> changePassword(@AuthenticationPrincipal UserDetails userDetails,
                                        @RequestBody @Valid ChangePasswordRequest request) {
        String email = userDetails.getUsername();
        boolean success = accountService.changePassword(
            email, 
            request.getOldPassword(), 
            request.getNewPassword()
        );
        
        if (!success) {
            return new ResponseData<>(HttpStatus.BAD_REQUEST.value(),
                    "Failed to change password. Please check your old password.", null);
        }
        
        return new ResponseData<>(HttpStatus.OK.value(),
                "Password changed successfully", null);
    }

    private String maskEmail(String email) {
        if (email == null) return "null";
        int at = email.indexOf('@');
        if (at <= 1) return "***" + email.substring(Math.max(at, 0));
        return email.charAt(0) + "***" + email.substring(at);
    }
}


