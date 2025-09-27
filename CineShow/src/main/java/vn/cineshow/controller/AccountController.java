package vn.cineshow.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.cineshow.dto.request.EmailRegisterRequest;
import vn.cineshow.dto.request.OtpVerifyDTO;
import vn.cineshow.enums.AccountStatus;
import vn.cineshow.repository.AccountRepository;
import vn.cineshow.service.OtpService;
import vn.cineshow.service.RegisterService;
import vn.cineshow.service.impl.EmailService;
import vn.cineshow.service.impl.TempRegisterServiceImpl;

import java.net.URI;
@RestController
@RequestMapping("/accounts")
@RequiredArgsConstructor
public class AccountController {
    private final RegisterService registerService;
    private final OtpService otpService;
    private final TempRegisterServiceImpl tempRegisterService;
    private final AccountRepository accountRepository;

    // B1: Nhận info, lưu Redis, gửi OTP
    @PostMapping("/register-email")
    public ResponseEntity<?> registerEmail(@RequestBody @Valid EmailRegisterRequest req) {
        if (!req.password().equals(req.confirmPassword())) {
            return ResponseEntity.badRequest().body("password != confirmPassword");
        }

        // Chặn nếu email đã có account ACTIVE
        if (accountRepository.findByEmailAndStatus(req.email(), AccountStatus.ACTIVE).isPresent()) {
            return ResponseEntity.badRequest().body("Email already exists");
        }

        // Lưu vào Redis tạm thời nếu chưa có email đó trg cache
        if(!tempRegisterService.emailExists(req.email())) {
            tempRegisterService.saveRegisterData(req);
        }

        // Gửi OTP
        otpService.sendOtp(req.email(), req.name());

        return ResponseEntity.accepted().body("OTP sent to " + req.email());
    }

    // B2: Verify OTP và tạo account
    @PostMapping("/verify-otp")
    public ResponseEntity<?> verifyOtp(@RequestBody @Valid OtpVerifyDTO req) {
        // 1) Check OTP
        otpService.verifyOtp(req.email(), req.otpCode());

        // 2) Lấy info từ Redis
        EmailRegisterRequest registerReq = tempRegisterService.getRegisterData(req.email());
        if (registerReq == null) {
            return ResponseEntity.badRequest().body("Register info expired or not found.");
        }

        // 3) Tạo account trong DB
        long id = registerService.registerByEmail(registerReq);

        // 4) Dọn Redis
        tempRegisterService.deleteRegisterData(req.email());

        return ResponseEntity.created(URI.create("/users/" + id)).build();
    }
}
