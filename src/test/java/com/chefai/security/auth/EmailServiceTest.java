package com.chefai.security.auth;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.chefai.security.user.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class EmailServiceTest {

    @Mock
    private JavaMailSender mailSender;

    private EmailService service;

    @BeforeEach
    void setUp() {
        service = new EmailService();
        ReflectionTestUtils.setField(service, "mailSender", mailSender);
        ReflectionTestUtils.setField(service, "from", "no-reply@example.com");
        ReflectionTestUtils.setField(service, "verificationUrl", "http://localhost:3000/verify-email");
    }

    @Test
    void doesNotSendEmailWhenDisabled() {
        ReflectionTestUtils.setField(service, "enabled", false);

        service.sendVerificationEmail(User.builder()
                .email("user@example.com")
                .verificationToken("token-123")
                .build());

        verify(mailSender, never()).send(org.mockito.ArgumentMatchers.any(SimpleMailMessage.class));
    }

    @Test
    void sendsVerificationEmailWhenEnabled() {
        ReflectionTestUtils.setField(service, "enabled", true);
        var user = User.builder()
                .email("user@example.com")
                .verificationToken("token-123")
                .build();

        service.sendVerificationEmail(user);

        var captor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(captor.capture());
        var message = captor.getValue();
        assertEquals("no-reply@example.com", message.getFrom());
        assertArrayEquals(new String[]{"user@example.com"}, message.getTo());
        assertEquals("Verify your email", message.getSubject());
        assertEquals(
                "Verify your account: http://localhost:3000/verify-email?token=token-123",
                message.getText());
    }
}
