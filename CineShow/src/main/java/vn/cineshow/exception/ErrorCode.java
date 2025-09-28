package vn.cineshow.exception;

import lombok.Getter;

@Getter
public enum ErrorCode {
    USER_EXISTED(1001, "User existed"),
    EMAIL_EXISTED(1002, "Email existed");


    private final int code;
    private final String message;

    ErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }
}