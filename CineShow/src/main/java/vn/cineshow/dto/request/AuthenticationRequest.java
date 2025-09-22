package vn.cineshow.dto.request;

import vn.cineshow.utils.validator.Email;
import vn.cineshow.utils.validator.Password;

public class AuthenticationRequest {
    @Email
    String email;

    @Password
    String password;
}
