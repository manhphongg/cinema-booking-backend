package vn.cineshow.dto.request;

import jakarta.validation.constraints.*;

public record SendOtpRequest(@NotBlank @Email String email) {}