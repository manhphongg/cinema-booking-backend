// vn/cineshow/dto/request/EmailRegisterRequest.java
package vn.cineshow.dto.request;

import jakarta.validation.constraints.*;
import java.time.LocalDate;
import vn.cineshow.enums.Gender;

public record EmailRegisterRequest(
        @NotBlank @Email String email,
        @NotBlank @Size(min=2,max=100) String name,
        @Past LocalDate dateOfBirth,
        @NotNull Gender gender,
        @NotBlank @Size(min=8,max=64) String password,
        @NotBlank @Size(min=8,max=64) String confirmPassword
) {}
