package com.chefai.security.auth;

import com.chefai.security.user.User;
import com.resend.Resend;
import com.resend.core.exception.ResendException;
import com.resend.services.emails.model.SendEmailRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    private final boolean enabled;
    private final String mode;
    private final String from;
    private final String verificationUrl;
    private final String passwordResetUrl;
    private final String resendApiKey;

    public EmailService() {
        this(false, "resend", "onboarding@resend.dev",
                "http://localhost:3000/verify-email",
                "http://localhost:3000/reset-password", "");
    }

    @Autowired(required = false)
    public EmailService(
            @Value("${application.mail.enabled:false}") boolean enabled,
            @Value("${application.mail.mode:resend}") String mode,
            @Value("${application.mail.from:onboarding@resend.dev}") String from,
            @Value("${application.mail.verification-url:http://localhost:3000/verify-email}") String verificationUrl,
            @Value("${application.mail.password-reset-url:http://localhost:3000/reset-password}") String passwordResetUrl,
            @Value("${RESEND_API_KEY:}") String resendApiKey) {
        this.enabled = enabled;
        this.mode = mode;
        this.from = from;
        this.verificationUrl = verificationUrl;
        this.passwordResetUrl = passwordResetUrl;
        this.resendApiKey = resendApiKey;
    }

    public void sendVerificationEmail(User user) {
        if (!enabled || user == null) {
            return;
        }

        var verificationLink = verificationUrl + "?token=" + user.getVerificationToken();
        sendEmail(user.getEmail(), "Verify your Chef AI account", verificationLink,
                """
                        <h2>Welcome to Chef AI!</h2>
                        <p>Please click the link below to verify your email:</p>
                        <a href="%s">Verify Email</a>
                        """.formatted(verificationLink));
    }

    public void sendPasswordResetEmail(User user, String token) {
        if (!enabled || user == null) {
            return;
        }

        var resetLink = passwordResetUrl + "?token=" + token;
        sendEmail(user.getEmail(), "Reset your Chef AI password", resetLink,
                """
                        <h2>Password reset requested</h2>
                        <p>Click the link below to set a new password:</p>
                        <a href="%s">Reset Password</a>
                        <p>This link expires in 30 minutes.</p>
                        """.formatted(resetLink));
    }

    private void sendEmail(String recipient, String subject, String link, String html) {
        if ("log".equalsIgnoreCase(mode)) {
            log.info("Email link for {}: {}", recipient, link);
            return;
        }

        if (!"resend".equalsIgnoreCase(mode)) {
            throw new IllegalStateException("Unsupported mail mode: " + mode);
        }

        if (resendApiKey.isBlank()) {
            throw new IllegalStateException("Mail is enabled but RESEND_API_KEY is not configured");
        }

        var resend = new Resend(resendApiKey);
        var params = SendEmailRequest.builder()
                .from(from)
                .to(recipient)
                .subject(subject)
                .html(html)
                .build();
        try {
            resend.emails().send(params);
        } catch (ResendException exception) {
            throw new IllegalStateException("Failed to send verification email", exception);
        }
    }
}
