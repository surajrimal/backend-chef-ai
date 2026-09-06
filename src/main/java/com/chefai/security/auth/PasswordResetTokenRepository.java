package com.chefai.security.auth;

import java.util.Optional;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface PasswordResetTokenRepository extends MongoRepository<PasswordResetToken, String> {

    Optional<PasswordResetToken> findByTokenHashAndUsedAtIsNull(String tokenHash);

    void deleteAllByUserIdAndUsedAtIsNull(String userId);
}
