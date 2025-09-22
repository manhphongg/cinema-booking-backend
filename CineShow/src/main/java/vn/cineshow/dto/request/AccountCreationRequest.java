package vn.cineshow.dto.request;


import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;
import vn.cineshow.utils.validator.Email;
import vn.cineshow.utils.validator.Password;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AccountCreationRequest {

    @Email
    @Schema(example = "user1@gmail.com", defaultValue = "user1@gmail.com")
    String email;

    @Password
    @Schema(example = "12345678", defaultValue = "12345678")
    String password;

    @NotNull(message = "Name must be not blank")
    @Schema(example = "Manh Phong", defaultValue = "Manh Phong")
    String name;

    @NotNull(message = "Address must be not blank")
    @Schema(example = "Ha Noi", defaultValue = "Ha Noi")
    String address;

}
