package com.chefai.security.token;

import java.util.List;
import java.util.Optional;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface TokenRepository extends MongoRepository<Token, String> {

  List<Token> findAllByUserIdAndExpiredIsFalseAndRevokedIsFalse(String userId);

  Optional<Token> findByToken(String token);

  Optional<Token> findByTokenAndTokenType(String token, TokenType tokenType);
}
