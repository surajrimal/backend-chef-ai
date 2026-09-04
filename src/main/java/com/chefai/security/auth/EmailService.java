package com.chefai.security.auth;

import com.chefai.security.user.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.lang.Nullable;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    private final JavaMailSender mailSender;
    private final boolean enabled;
    private final String mode;
    private final String from;
    private final String verificationUrl;

    public EmailService() {
        this(null, true, "smtp", "no-reply@example.com", "http://localhost:3000/verify-email");
    }

    @Autowired(required = false)
    public EmailService(
            @Nullable JavaMailSender mailSender,
            @Value("${application.mail.enabled:false}") boolean enabled,
            @Value("${application.mail.mode:smtp}") String mode,
            @Value("${application.mail.from:no-reply@example.com}") String from,
            @Value("${application.mail.verification-url:http://localhost:3000/verify-email}") String verificationUrl) {
        this.mailSender = mailSender;
        this.enabled = enabled;
        this.mode = mode;
        this.from = from;
        this.verificationUrl = verificationUrl;
    }

    public void sendVerificationEmail(User user) {
        if (!enabled || user == null) {
            return;
        }

        var verificationLink = verificationUrl + "?token=" + user.getVerificationToken();
        if ("log".equalsIgnoreCase(mode)) {
            log.info("Email verification link for {}: {}", user.getEmail(), verificationLink);
            return;
        }

        if (!"smtp".equalsIgnoreCase(mode)) {
            throw new IllegalStateException("Unsupported mail mode: " + mode);
        }

        if (mailSender == null) {
            throw new IllegalStateException("Mail is enabled but no JavaMailSender is configured");
        }

        var message = new SimpleMailMessage();
        message.setFrom(from);
        message.setTo(user.getEmail());
        message.setSubject("Verify your email");
        message.setText("Verify your account: " + verificationLink);
        mailSender.send(message);
    }
}
