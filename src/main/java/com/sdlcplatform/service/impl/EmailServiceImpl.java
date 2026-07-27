package com.sdlcplatform.service.impl;

import com.sdlcplatform.config.AppProperties;
import com.sdlcplatform.service.EmailService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;
    private final AppProperties appProperties;

    @Override
    @Async
    public void sendVerificationEmail(String toEmail, String fullName, String verificationLink) {
        String body = """
                <p>Hi %s,</p>
                <p>Thanks for registering on the AI SDLC Management Platform. Please confirm your email address:</p>
                <p><a href="%s">Verify Email</a></p>
                <p>This link expires in 24 hours.</p>
                """.formatted(fullName, verificationLink);
        send(toEmail, "Verify your email", body);
    }

    @Override
    @Async
    public void sendPasswordResetEmail(String toEmail, String fullName, String resetLink) {
        String body = """
                <p>Hi %s,</p>
                <p>We received a request to reset your password:</p>
                <p><a href="%s">Reset Password</a></p>
                <p>This link expires in 30 minutes. If you didn't request this, ignore this email.</p>
                """.formatted(fullName, resetLink);
        send(toEmail, "Reset your password", body);
    }

    private void send(String toEmail, String subject, String htmlBody) {
        log.info("Attempting to send email '{}' to {}", subject, toEmail);
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, "utf-8");
            helper.setFrom(appProperties.getMail().getFrom());
            helper.setTo(toEmail);
            helper.setSubject(subject);
            helper.setText(htmlBody, true);
            mailSender.send(message);
            log.info("Successfully sent email '{}' to {}", subject, toEmail);
        } catch (Exception e) {
            log.error("Failed to send email '{}' to {}: {}", subject, toEmail, e.getMessage(), e);
        }
    }
}
