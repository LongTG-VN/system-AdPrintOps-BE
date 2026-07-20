package com.adprintops.auth;

public class InvalidCredentialsException extends RuntimeException {
    public InvalidCredentialsException() {
        super("Email hoặc mật khẩu không chính xác.");
    }
}
