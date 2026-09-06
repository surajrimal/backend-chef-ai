package com.chefai.security.auth;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthenticationController {

  private final AuthenticationService service;
  private final PasswordResetService passwordResetService;

  @PostMapping("/register")
  public ResponseEntity<AuthenticationResponse> register(
      @RequestBody RegisterRequest request
  ) {
    return ResponseEntity.ok(service.register(request));
  }
  @PostMapping("/authenticate")
  public ResponseEntity<AuthenticationResponse> authenticate(
      @RequestBody AuthenticationRequest request
  ) {
    return ResponseEntity.ok(service.authenticate(request));
  }

  @PostMapping("/forgot-password")
  public ResponseEntity<Void> forgotPassword(
      @RequestBody ForgotPasswordRequest request
  ) {
    passwordResetService.requestReset(request == null ? null : request.getEmail());
    return ResponseEntity.accepted().build();
  }

  @PostMapping("/reset-password")
  public ResponseEntity<Void> resetPassword(
      @RequestBody ResetPasswordRequest request
  ) {
    passwordResetService.resetPassword(request);
    return ResponseEntity.noContent().build();
  }

  @GetMapping("/verify-email")
  public ResponseEntity<Void> verifyEmail(@RequestParam String token) {
    service.verifyEmail(token);
    return ResponseEntity.noContent().build();
  }

  @PostMapping("/refresh-token")
  public void refreshToken(
      HttpServletRequest request,
      HttpServletResponse response
  ) throws IOException {
    service.refreshToken(request, response);
  }


}
