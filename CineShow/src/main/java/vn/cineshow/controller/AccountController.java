package vn.cineshow.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import vn.cineshow.dto.request.EmailRegisterRequest;
import vn.cineshow.dto.response.ResponseData;
import vn.cineshow.exception.IllegalParameterException;
import vn.cineshow.service.RegisterService;

@RestController
@RequestMapping("/accounts")
@RequiredArgsConstructor
public class AccountController {
    private final RegisterService registerService;

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
}


