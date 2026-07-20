package com.adprintops.feedback;

import jakarta.validation.constraints.NotBlank;

public record FeedbackRequest(
        @NotBlank(message = "Nội dung góp ý không được để trống")
        String reasonDetails,

        String recipientEmail,

        String fileName,

        String imageDataBase64
) {}
