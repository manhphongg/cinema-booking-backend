package vn.cineshow.dto.request;

import jakarta.validation.constraints.*;

public record VerifyOtpRequest(
        @NotBlank @Email String email,
        @NotBlank @Size(min=4,max=8) String otp
) {}
