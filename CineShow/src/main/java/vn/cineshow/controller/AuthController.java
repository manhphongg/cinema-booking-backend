package vn.cineshow.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.cineshow.dto.request.ChangePasswordRequest;
import vn.cineshow.dto.request.ForgotPasswordRequest;
import vn.cineshow.service.AuthService;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @PostMapping("/forgot-password")
    public String forgotPassword(@RequestBody ForgotPasswordRequest request) {
        boolean success = authService.forgotPassword(request);
        return success ? "Password updated." : "Account not found.";
    }

    @PutMapping("/change-password")
    public String changePassword(@RequestBody ChangePasswordRequest request) {
        boolean success = authService.changePassword(request);
        return success ? "Password changed successfully." : "Invalid credentials.";
    }
}
