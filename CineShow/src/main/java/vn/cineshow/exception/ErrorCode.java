package vn.cineshow.exception;

import lombok.Getter;

@Getter
public enum ErrorCode {
    USER_EXISTED(1001, "User already exists"),
    EMAIL_EXISTED(1002, "Email already exists"),
    EMAIL_UN_VERIFIED(1003, "Email has not been verified"),
    ACCOUNT_INACTIVE(1004, "Your account has been blocked or is inactive"),
    OTP_NOT_FOUND(1005, "OTP not found"),
    OTP_EXPIRED(1006, "OTP has expired"),
    OTP_INVALID(1007, "OTP is invalid"),
    ;

    private final int code;
    private final String message;

    ErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }
}