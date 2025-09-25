// vn/cineshow/controller/OtpController.java
package vn.cineshow.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.cineshow.dto.request.SendOtpRequest;
import vn.cineshow.dto.request.VerifyOtpRequest;
import vn.cineshow.service.OtpService;

@RestController
@RequestMapping("/otp")
@RequiredArgsConstructor
public class OtpController {
    private final OtpService otpService;

    @PostMapping("/send")
    public ResponseEntity<?> send(@RequestBody @Valid SendOtpRequest req) {
        otpService.sendOtp(req.email());
        return ResponseEntity.accepted().build(); // 202 Accepted
    }

    @PostMapping("/verify")
    public ResponseEntity<?> verify(@RequestBody @Valid VerifyOtpRequest req) {
        otpService.verifyOtp(req.email(), req.otp());
        return ResponseEntity.ok().build();
    }
}
