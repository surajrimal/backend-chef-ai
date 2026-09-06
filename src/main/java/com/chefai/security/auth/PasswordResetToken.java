package com.chefai.security.auth;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "password_reset_tokens")
public class PasswordResetToken {

    @Id
    private String id;

    @Indexed(unique = true)
    private String tokenHash;

    private String userId;
    private LocalDateTime expiresAt;
    private LocalDateTime usedAt;
}
