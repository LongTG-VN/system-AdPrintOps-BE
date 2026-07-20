package com.adprintops.feedback;

import jakarta.mail.internet.MimeMessage;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.Base64;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/v1/feedback")
public class FeedbackController {

    private static final Logger log = LoggerFactory.getLogger(FeedbackController.class);
    private static final String DEFAULT_RECIPIENT = "gialong.game@gmail.com";

    private final JavaMailSender mailSender;

    public FeedbackController(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @PostMapping
    public ResponseEntity<FeedbackResponse> submitFeedback(@Valid @RequestBody FeedbackRequest request) {
        String targetEmail = (request.recipientEmail() != null && !request.recipientEmail().isBlank())
                ? request.recipientEmail()
                : DEFAULT_RECIPIENT;

        log.info("==========================================================");
        log.info(">>> QUEUING REAL EMAIL VIA GMAIL SMTP TO: {}", targetEmail);
        log.info("Details (Input/Output/Reason):\n{}", request.reasonDetails());

        CompletableFuture.runAsync(() -> {
            try {
                MimeMessage message = mailSender.createMimeMessage();
                MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

                helper.setFrom("gialong.game@gmail.com");
                helper.setTo(targetEmail);
                helper.setSubject("[BÁO CÁO GÓP Ý & BÁO LỖI] Bảng Báo Giá AdPrintOps");

                StringBuilder content = new StringBuilder();
                content.append("<h2>HỆ THỐNG PHẢN HỒI ADPRINTOPS</h2>");
                content.append("<p><strong>Thời gian gửi:</strong> ").append(Instant.now()).append("</p>");
                content.append("<hr/>");
                content.append("<h3>THÔNG TIN CHI TIẾT (INPUT / OUTPUT / LÝ DO):</h3>");
                content.append("<pre style='background:#f4f4f4; padding:12px; border-radius:6px; font-family:monospace;'>")
                       .append(request.reasonDetails())
                       .append("</pre>");

                if (request.fileName() != null && !request.fileName().isBlank()) {
                    content.append("<p><strong>File đính kèm:</strong> ").append(request.fileName()).append("</p>");
                }

                helper.setText(content.toString(), true);

                // Attach image if base64 provided
                if (request.imageDataBase64() != null && request.imageDataBase64().contains(",")) {
                    String base64Data = request.imageDataBase64().split(",")[1];
                    byte[] imageBytes = Base64.getDecoder().decode(base64Data);
                    String fileName = request.fileName() != null && !request.fileName().isBlank() ? request.fileName() : "dinh_kem.png";
                    helper.addAttachment(fileName, new ByteArrayResource(imageBytes));
                }

                mailSender.send(message);
                log.info(">>> SUCCESS: Real email sent via Google SMTP to {}", targetEmail);
            } catch (Exception e) {
                log.error("Failed to send real email via Google SMTP: {}", e.getMessage(), e);
            }
        });

        log.info("==========================================================");

        String responseMessage = "Góp ý đã được tiếp nhận và đang gửi tới email " + targetEmail + "!";
        return ResponseEntity.ok(new FeedbackResponse(true, responseMessage, Instant.now()));
    }
}
