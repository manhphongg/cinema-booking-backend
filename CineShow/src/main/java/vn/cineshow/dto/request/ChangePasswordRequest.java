package vn.cineshow.dto.request;

import lombok.*;
import vn.cineshow.utils.validator.Password;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChangePasswordRequest {
    @Password
    private String oldPassword;

    @Password
    private String newPassword;

}
