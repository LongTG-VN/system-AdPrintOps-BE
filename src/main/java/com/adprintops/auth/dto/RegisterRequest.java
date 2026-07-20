package com.adprintops.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @NotBlank(message = "Email không được trống")
        @Email(message = "Email không hợp lệ")
        @Size(max = 254, message = "Email không được vượt quá 254 ký tự")
        String email,

        @NotBlank(message = "Mật khẩu không được trống")
        @Size(min = 8, max = 72, message = "Mật khẩu phải có từ 8 đến 72 ký tự")
        String password,

        @NotBlank(message = "Tên hiển thị không được trống")
        @Size(max = 100, message = "Tên hiển thị không được vượt quá 100 ký tự")
        String displayName
) {
}
