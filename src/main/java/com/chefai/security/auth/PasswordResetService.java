package com.chefai.security.auth;

import com.chefai.security.token.TokenRepository;
import com.chefai.security.user.User;
import com.chefai.security.user.UserRepository;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PasswordResetService {

    private static final long RESET_TOKEN_EXPIRATION_MINUTES = 30;

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository resetTokenRepository;
    private final TokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final SecureRandom secureRandom = new SecureRandom();

    public void requestReset(String email) {
        if (email == null || email.isBlank()) {
            return;
        }

        userRepository.findByEmail(email.trim()).ifPresent(user -> {
            resetTokenRepository.deleteAllByUserIdAndUsedAtIsNull(user.getId());

            var rawToken = generateToken();
            resetTokenRepository.save(PasswordResetToken.builder()
                    .tokenHash(hash(rawToken))
                    .userId(user.getId())
                    .expiresAt(LocalDateTime.now().plusMinutes(RESET_TOKEN_EXPIRATION_MINUTES))
                    .build());
            emailService.sendPasswordResetEmail(user, rawToken);
        });
    }

    public void resetPassword(ResetPasswordRequest request) {
        if (request == null || request.getToken() == null || request.getToken().isBlank()) {
            throw new IllegalArgumentException("Invalid password reset token");
        }
        if (request.getNewPassword() == null
                || !request.getNewPassword().equals(request.getConfirmationPassword())) {
            throw new IllegalArgumentException("Passwords are not the same");
        }

        var resetToken = resetTokenRepository
                .findByTokenHashAndUsedAtIsNull(hash(request.getToken()))
                .orElseThrow(() -> new IllegalArgumentException("Invalid password reset token"));
        if (resetToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Password reset token has expired");
        }

        var user = userRepository.findById(resetToken.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("Invalid password reset token"));
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        resetToken.setUsedAt(LocalDateTime.now());
        resetTokenRepository.save(resetToken);
        revokeAllUserTokens(user);
    }

    private void revokeAllUserTokens(User user) {
        var validTokens = tokenRepository
                .findAllByUserIdAndExpiredIsFalseAndRevokedIsFalse(user.getId());
        validTokens.forEach(token -> {
            token.setExpired(true);
            token.setRevoked(true);
        });
        tokenRepository.saveAll(validTokens);
    }

    private String generateToken() {
        var bytes = new byte[32];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private String hash(String token) {
        try {
            return Base64.getUrlEncoder().withoutPadding().encodeToString(
                    MessageDigest.getInstance("SHA-256")
                            .digest(token.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("Unable to hash password reset token", exception);
        }
    }
}
