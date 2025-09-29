package vn.cineshow.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.web.bind.annotation.*;
import vn.cineshow.dto.request.SignInRequest;
import vn.cineshow.dto.response.ResponseData;
import vn.cineshow.dto.response.SignInResponse;
import vn.cineshow.dto.response.TokenResponse;
import vn.cineshow.service.AuthenticationService;
import vn.cineshow.service.OtpService;
import vn.cineshow.service.UserService;

import java.time.Duration;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
@Tag(name = "Authentication Controller")
@Slf4j(topic = "AUTHENTICATION-CONTROLLER")
public class AuthenticationController {

    private final AuthenticationService authenticationService;

    private final UserService userService;

    private final OtpService otpService;

    @Operation(summary = "Access token", description = "Get access token  email and password")
    @PostMapping("/log-in")
    public ResponseData<SignInResponse> getAccessToken(@RequestBody @Valid SignInRequest req, HttpServletResponse response) {
        log.info("Access token request:");
        TokenResponse tokenResponse = authenticationService.signIn(req);

        //set cookie for refresh token
        ResponseCookie cookie = ResponseCookie.from("refreshToken", tokenResponse.getRefreshToken())
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(Duration.ofDays(30))
                .sameSite("Strict")
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());

        log.info("Access token response:" + tokenResponse.getAccessToken());
        //return access token + account info
        return new ResponseData<>(HttpStatus.OK.value(),
                "Login successful",
                SignInResponse.builder()
                        .accessToken(tokenResponse.getAccessToken())
                        .roleName(tokenResponse.getRoleName())
                        .email(tokenResponse.getEmail())
                        .userId(tokenResponse.getUserId())
                        .build()
        );
    }

    @Operation(summary = "Refresh access token", description = "Get access token by refresh token when access token expired")
    @PostMapping("/refresh-token")
    public ResponseData<SignInResponse> refreshAccessToken(@CookieValue("refreshToken") String refreshToken) {
        log.info("Get new access token request:");
        SignInResponse tokenResponse = authenticationService.refresh(refreshToken);
        log.info("Generated new access token for user {}: {}", tokenResponse.getEmail(), tokenResponse.getAccessToken());
        return new ResponseData<>(HttpStatus.OK.value(),
                "Token refreshed successfully",
                tokenResponse
        );
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

    @Operation(summary = "Verify account", description = "Verify account with token from email")
    @PostMapping("/verify")
    public ResponseData<String> verifyAccount(@RequestParam String code, @RequestParam String email) {
        log.info("Verify account request with token: {}", code);

        otpService.verifyOtp(code, email);
        return new ResponseData<>(HttpStatus.OK.value(),
                "Account verified successfully"
        );
    }
}
