package com.adprintops.user;

public class UserNotFoundException extends RuntimeException {
    public UserNotFoundException() {
        super("Không tìm thấy người dùng.");
    }
}
