package com.adprintops.feedback;

import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;

@RestController
@RequestMapping("/api/v1/feedback")
public class FeedbackController {

    private static final Logger log = LoggerFactory.getLogger(FeedbackController.class);

    private final EmailService emailService;
    private final String defaultRecipient;

    public FeedbackController(
            EmailService emailService,
            @Value("${app.feedback.default-recipient:longtg.ce191181@gmail.com}") String defaultRecipient
    ) {
        this.emailService = emailService;
        this.defaultRecipient = defaultRecipient;
    }

    @GetMapping("/test-mail")
    public ResponseEntity<String> testMail() {
        try {
            emailService.sendEmail(
                    defaultRecipient,
                    "[TEST DIRECT MAIL] AdPrintOps Backend",
                    "<h2>Test direct email sending from Render backend via Resend API.</h2>",
                    null,
                    null
            );
            return ResponseEntity.ok("SUCCESS: Email sent to " + defaultRecipient);
        } catch (Exception e) {
            log.error("Test mail error: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body("ERROR: " + e.getClass().getName() + ": " + e.getMessage());
        }
    }

    @PostMapping
    public ResponseEntity<FeedbackResponse> submitFeedback(@Valid @RequestBody FeedbackRequest request) {
        String targetEmail = (request.recipientEmail() != null && !request.recipientEmail().isBlank())
                ? request.recipientEmail()
                : defaultRecipient;

        log.info("==========================================================");
        log.info(">>> QUEUING REAL EMAIL TO: {}", targetEmail);
        log.info("Details (Input/Output/Reason):\n{}", request.reasonDetails());

        try {
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

                emailService.sendEmail(
                        targetEmail,
                        "[BÁO CÁO GÓP Ý & BÁO LỖI] Bảng Báo Giá AdPrintOps",
                        content.toString(),
                        request.fileName(),
                        request.imageDataBase64()
                );
                log.info(">>> SUCCESS: Real email dispatched to {}", targetEmail);
        } catch (Exception e) {
            log.error("Failed to send real email: {}", e.getMessage(), e);
            throw new IllegalStateException("Không thể gửi email góp ý. Vui lòng thử lại sau.", e);
        }

        log.info("==========================================================");

        String responseMessage = "Góp ý đã được gửi tới email " + targetEmail + ".";
        return ResponseEntity.ok(new FeedbackResponse(true, responseMessage, Instant.now()));
    }
}
