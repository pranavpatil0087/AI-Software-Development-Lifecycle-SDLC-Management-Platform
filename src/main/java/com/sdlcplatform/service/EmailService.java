package com.sdlcplatform.service;

/**
 * Abstraction over the mail transport so AuthService never depends directly
 * on JavaMailSender. Swapping Mailtrap (dev) for Resend (prod) is purely
 * an application.yml / environment-variable concern — this contract doesn't change.
 */
public interface EmailService {
    void sendVerificationEmail(String toEmail, String fullName, String verificationLink);
    void sendPasswordResetEmail(String toEmail, String fullName, String resetLink);
}
