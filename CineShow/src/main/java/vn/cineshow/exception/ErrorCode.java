package vn.cineshow.exception;

import lombok.Getter;

@Getter
public enum ErrorCode {
    USER_EXISTED(1001, "User existed"),
    EMAIL_EXISTED(1002, "Email existed"),
    EMAIL_UN_VERIFIED(1003, "Email has not been verified"),
    ACCOUNT_INACTIVE(1004, "Account inactive"),
    INVALID_CREDENTIALS(1005, "Invalid email or password"),
    OTP_NOT_FOUND(1006, "OTP not found"),
    OTP_INVALID(1007, "Invalid OTP"),
    OTP_EXPIRED(1008, "OTP expired");


    private final int code;
    private final String message;

    ErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }
}