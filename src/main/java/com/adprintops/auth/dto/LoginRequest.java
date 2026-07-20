package com.adprintops.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record LoginRequest(
        @NotBlank(message = "Email không được trống")
        @Email(message = "Email không hợp lệ")
        String email,

        @NotBlank(message = "Mật khẩu không được trống")
        @Size(max = 72, message = "Mật khẩu không được vượt quá 72 ký tự")
        String password
) {
}
