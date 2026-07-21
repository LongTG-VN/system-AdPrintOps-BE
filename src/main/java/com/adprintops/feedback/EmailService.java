package com.adprintops.feedback;

import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    private final JavaMailSender mailSender;
    private final String resendApiKey;
    private final String resendFrom;
    private final RestTemplate restTemplate;

    public EmailService(
            JavaMailSender mailSender,
            @Value("${app.resend.api-key:}") String resendApiKey,
            @Value("${app.resend.from:}") String resendFrom
    ) {
        this.mailSender = mailSender;
        this.resendApiKey = resendApiKey;
        this.resendFrom = resendFrom;
        this.restTemplate = new RestTemplate();
    }

    public void sendEmail(String toEmail, String subject, String htmlBody, String fileName, String imageBase64) {
        // 1. Try Resend HTTPS REST API (Port 443 - Never blocked on Cloud providers)
        if (resendApiKey != null && !resendApiKey.isBlank()) {
            try {
                sendViaResend(toEmail, subject, htmlBody, fileName, imageBase64);
                return;
            } catch (Exception e) {
                log.error("Failed to send via Resend HTTPS API, falling back to SMTP: {}", e.getMessage());
            }
        }

        // SMTP is retained for local development only. Render Free blocks SMTP ports.
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom("gialong.game@gmail.com");
            helper.setTo(toEmail);
            helper.setSubject(subject);
            helper.setText(htmlBody, true);

            if (imageBase64 != null && imageBase64.contains(",")) {
                String base64Data = imageBase64.split(",")[1];
                byte[] imageBytes = Base64.getDecoder().decode(base64Data);
                String name = (fileName != null && !fileName.isBlank()) ? fileName : "dinh_kem.png";
                helper.addAttachment(name, new ByteArrayResource(imageBytes));
            }

            mailSender.send(message);
            log.info(">>> SUCCESS: Email sent via SMTP to {}", toEmail);
        } catch (Exception e) {
            log.error("Failed to send via SMTP: {}", e.getMessage(), e);
            throw new RuntimeException("Email delivery failed via both HTTPS API and SMTP: " + e.getMessage(), e);
        }
    }

    private void sendViaResend(String toEmail, String subject, String htmlBody, String fileName, String imageBase64) {
        String url = "https://api.resend.com/emails";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(resendApiKey);
        headers.set("User-Agent", "AdPrintOps/1.0");

        Map<String, Object> body = new HashMap<>();
        if (resendFrom == null || resendFrom.isBlank()) {
            throw new IllegalStateException("RESEND_FROM phải là email thuộc domain đã verify trên Resend");
        }
        body.put("from", resendFrom);
        body.put("to", List.of(toEmail));
        body.put("subject", subject);
        body.put("html", htmlBody);

        if (imageBase64 != null && imageBase64.contains(",")) {
            String base64Data = imageBase64.split(",")[1];
            String name = (fileName != null && !fileName.isBlank()) ? fileName : "dinh_kem.png";

            Map<String, String> attachment = new HashMap<>();
            attachment.put("filename", name);
            attachment.put("content", base64Data);
            body.put("attachments", List.of(attachment));
        }

        HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(body, headers);
        ResponseEntity<String> response = restTemplate.postForEntity(url, requestEntity, String.class);

        if (response.getStatusCode().is2xxSuccessful()) {
            log.info(">>> SUCCESS: Email sent via Resend HTTPS API (Port 443) to {}: {}", toEmail, response.getBody());
        } else {
            throw new RuntimeException("Resend API returned status " + response.getStatusCode() + ": " + response.getBody());
        }
    }
}
