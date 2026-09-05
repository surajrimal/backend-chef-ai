package com.chefai.security.auth;

import com.chefai.security.user.User;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class EmailServiceTest {

    @Test
    void doesNotSendEmailWhenDisabled() {
        var service = new EmailService();
        ReflectionTestUtils.setField(service, "enabled", false);

        service.sendVerificationEmail(User.builder()
                .email("user@example.com")
                .verificationToken("token-123")
                .build());
    }

    @Test
    void logsVerificationLinkInLocalModeWithoutSendingEmail() {
        var service = new EmailService();
        ReflectionTestUtils.setField(service, "enabled", true);
        ReflectionTestUtils.setField(service, "mode", "log");

        service.sendVerificationEmail(User.builder()
                .email("user@example.com")
                .verificationToken("token-123")
                .build());
    }
}
