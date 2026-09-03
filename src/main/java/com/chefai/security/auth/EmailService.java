package com.chefai.security.auth;

import com.chefai.security.user.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired(required = false)
    private JavaMailSender mailSender;

    @Value("${application.mail.enabled:false}")
    private boolean enabled;
    @Value("${application.mail.from:no-reply@example.com}")
    private String from;
    @Value("${application.mail.verification-url:http://localhost:3000/verify-email}")
    private String verificationUrl;

    public void sendVerificationEmail(User user) {
        if (!enabled) {
            return;
        }

        var message = new SimpleMailMessage();
        message.setFrom(from);
        message.setTo(user.getEmail());
        message.setSubject("Verify your email");
        message.setText("Verify your account: " + verificationUrl
                + "?token=" + user.getVerificationToken());
        if (mailSender == null) {
            throw new IllegalStateException("Mail is enabled but no JavaMailSender is configured");
        }
        mailSender.send(message);
    }
}
