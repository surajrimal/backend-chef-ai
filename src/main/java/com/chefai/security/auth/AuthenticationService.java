package com.chefai.security.auth;

import com.chefai.security.config.JwtService;
import com.chefai.security.token.Token;
import com.chefai.security.token.TokenRepository;
import com.chefai.security.token.TokenType;
import com.chefai.security.user.User;
import com.chefai.security.user.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthenticationService {
  private final UserRepository repository;
  private final TokenRepository tokenRepository;
  private final PasswordEncoder passwordEncoder;
  private final JwtService jwtService;
  private final AuthenticationManager authenticationManager;
  private final EmailService emailService;

  public AuthenticationResponse register(RegisterRequest request) {
    if (repository.existsByEmail(request.getEmail())) {
      throw new IllegalStateException("User with email already exists");
    }

    var user = User.builder()
        .firstname(request.getFirstname())
        .lastname(request.getLastname())
        .email(request.getEmail())
        .password(passwordEncoder.encode(request.getPassword()))
        .role(com.chefai.security.user.Role.USER)
        .enabled(false)
        .verificationToken(UUID.randomUUID().toString())
        .verificationTokenExpiresAt(LocalDateTime.now().plusHours(24))
        .build();
    var savedUser = repository.save(user);
    emailService.sendVerificationEmail(savedUser);
    return AuthenticationResponse.builder().build();
  }

  public void verifyEmail(String token) {
    var user = repository.findByVerificationToken(token)
        .orElseThrow(() -> new IllegalArgumentException("Invalid verification token"));
    if (user.getVerificationTokenExpiresAt() == null
        || user.getVerificationTokenExpiresAt().isBefore(LocalDateTime.now())) {
      throw new IllegalArgumentException("Verification token has expired");
    }
    user.setEnabled(true);
    user.setVerificationToken(null);
    user.setVerificationTokenExpiresAt(null);
    repository.save(user);
  }

  public AuthenticationResponse authenticate(AuthenticationRequest request) {
    authenticationManager.authenticate(
        new UsernamePasswordAuthenticationToken(
            request.getEmail(),
            request.getPassword()
        )
    );
    var user = repository.findByEmail(request.getEmail())
        .orElseThrow();
    var jwtToken = jwtService.generateToken(user);
    var refreshToken = jwtService.generateRefreshToken(user);
    revokeAllUserTokens(user);
    saveUserToken(user, jwtToken);
    saveRefreshToken(user, refreshToken);
    return AuthenticationResponse.builder()
        .accessToken(jwtToken)
            .refreshToken(refreshToken)
        .build();
  }

  private void saveUserToken(User user, String jwtToken) {
    saveToken(user, jwtToken, TokenType.BEARER);
  }

  private void saveRefreshToken(User user, String refreshToken) {
    saveToken(user, refreshToken, TokenType.REFRESH);
  }

  private void saveToken(User user, String tokenValue, TokenType tokenType) {
    var token = Token.builder()
        .userId(user.getId())
        .token(tokenValue)
        .tokenType(tokenType)
        .expired(false)
        .revoked(false)
        .build();
    tokenRepository.save(token);
  }

  private void revokeAllUserTokens(User user) {
    var validUserTokens = tokenRepository.findAllByUserIdAndExpiredIsFalseAndRevokedIsFalse(user.getId());
    if (validUserTokens.isEmpty())
      return;
    validUserTokens.forEach(token -> {
      token.setExpired(true);
      token.setRevoked(true);
    });
    tokenRepository.saveAll(validUserTokens);
  }

  public void refreshToken(
          HttpServletRequest request,
          HttpServletResponse response
  ) throws IOException {
    final String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
    final String refreshToken;
    if (authHeader == null ||!authHeader.startsWith("Bearer ")) {
      response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Refresh token is required");
      return;
    }
    refreshToken = authHeader.substring(7);
    var storedRefreshToken = tokenRepository
        .findByTokenAndTokenType(refreshToken, TokenType.REFRESH)
        .filter(token -> !token.isExpired() && !token.isRevoked())
        .orElse(null);
    if (storedRefreshToken == null) {
      response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid refresh token");
      return;
    }

    var user = repository.findById(storedRefreshToken.getUserId())
        .orElse(null);
    if (user == null || !jwtService.isTokenValid(refreshToken, user, TokenType.REFRESH)) {
      response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid refresh token");
      return;
    }

    var accessToken = jwtService.generateToken(user);
    var rotatedRefreshToken = jwtService.generateRefreshToken(user);
    revokeAllUserTokens(user);
    saveUserToken(user, accessToken);
    saveRefreshToken(user, rotatedRefreshToken);
    var authResponse = AuthenticationResponse.builder()
        .accessToken(accessToken)
        .refreshToken(rotatedRefreshToken)
        .build();
    new ObjectMapper().writeValue(response.getOutputStream(), authResponse);
  }
}
