package vn.cineshow.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record OtpVerifyDTO(
        @NotBlank @Email String email,
        @NotBlank @Pattern(regexp = "\\d{6}")
        String otpCode
) {}
