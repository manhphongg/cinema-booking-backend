package vn.cineshow.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.web.bind.annotation.*;
import vn.cineshow.dto.request.AccountCreationRequest;
import vn.cineshow.dto.request.SignInRequest;
import vn.cineshow.dto.response.ResponseData;
import vn.cineshow.dto.response.SignInResponse;
import vn.cineshow.dto.response.TokenResponse;
import vn.cineshow.service.AuthenticationService;
import vn.cineshow.service.impl.EmailService;

import java.time.Duration;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
@Tag(name = "Authentication Controller")
@Slf4j(topic = "AUTHENTICATION-CONTROLLER")
public class AuthenticationController {

    private final AuthenticationService authenticationService;
    private final EmailService emailService;

    @Operation(summary = "Access token", description = "Get access token and refresh token by email and password")
    @PostMapping("/log-in")
    public ResponseData<?> getAccessToken(@RequestBody @Valid SignInRequest req, HttpServletResponse response) {
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

    @PostMapping("/register")
    public ResponseData<Long> register(@RequestBody @Valid AccountCreationRequest req) {
        log.info("Account creation request: {}", req);
        return new ResponseData<>(HttpStatus.CREATED.value(), "User successfully registered", authenticationService.accountRegister(req));
    }

    @PostMapping("/forgot-password")
    public ResponseData<Long> forgotPassword(@RequestParam @Valid String email) {
        log.info("Forgot password request: {}", email);
        authenticationService.forgotPassword(email);
        return new ResponseData<>(HttpStatus.CREATED.value(), "Email sent");
    }

    @PostMapping("/send")
    public ResponseData<Long> sendEmail(@RequestParam @Valid String email, @RequestParam @Valid String subject, @RequestParam @Valid String text) {
        log.info("Forgot password request: {}", email);
        
        emailService.send(email, subject, text);
        return new ResponseData<>(HttpStatus.CREATED.value(), "Email sent");
    }


}
