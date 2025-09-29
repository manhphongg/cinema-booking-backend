package vn.cineshow.controller;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import vn.cineshow.dto.request.EmailRegisterRequest;
import vn.cineshow.dto.response.ResponseData;
import vn.cineshow.exception.IllegalParameterException;
import vn.cineshow.service.OtpService;
import vn.cineshow.service.RegisterService;
import vn.cineshow.service.UserService;

@RestController
@RequestMapping("/accounts")
@RequiredArgsConstructor
@Slf4j
public class AccountController {
    private final RegisterService registerService;

    private final OtpService otpService;

    private final UserService userService;

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
    @PostMapping("/verify-otp")
    public ResponseData<?> verifyOtp(@RequestParam String email, @RequestParam String otp) {
        registerService.verifyAccountAndUpdateStatus(email, otp);
        return new ResponseData<>(HttpStatus.OK.value(),
                "Account activated successfully", null);
    }

    @Operation(summary = "Resend verification email", description = "Resend account verification email")
    @PostMapping("/resend-verification")
    public ResponseData<String> resendVerification(@RequestParam @Email String email) {
        log.info("Resend verification email request: {}", email);
        String name = userService.getNameByAccountEmail(email);
        otpService.sendOtp(email, name);
        return new ResponseData<>(HttpStatus.OK.value(),
                "Verification email has been resent",
                email
        );
    }
}


