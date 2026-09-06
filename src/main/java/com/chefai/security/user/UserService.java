package com.chefai.security.user;

import com.chefai.security.book.BookRepository;
import com.chefai.security.history.HistoryRepository;
import com.chefai.security.token.TokenRepository;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.Principal;

@Service
@RequiredArgsConstructor
public class UserService {

    private final PasswordEncoder passwordEncoder;
    private final UserRepository repository;
    private final BookRepository bookRepository;
    private final HistoryRepository historyRepository;
    private final TokenRepository tokenRepository;
    private final ArchivedAccountRepository archivedAccountRepository;

    public void changePassword(ChangePasswordRequest request, Principal connectedUser) {

        var user = (User) ((UsernamePasswordAuthenticationToken) connectedUser).getPrincipal();

        // check if the current password is correct
        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new IllegalStateException("Wrong password");
        }
        // check if the two new passwords are the same
        if (!request.getNewPassword().equals(request.getConfirmationPassword())) {
            throw new IllegalStateException("Password are not the same");
        }

        // update the password
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));

        // save the new password
        repository.save(user);
    }

    public void deleteAccount(String userId) {
        var user = repository.findById(userId)
                .orElseThrow(() -> new IllegalStateException("User account not found"));
        var books = bookRepository.findAllByCreatedBy(userId);
        var histories = historyRepository.findAllByLastModifiedBy(userId);
        var tokens = tokenRepository.findAllByUserId(userId);

        archivedAccountRepository.save(ArchivedAccount.builder()
                .id(UUID.randomUUID().toString())
                .userId(userId)
                .archivedAt(LocalDateTime.now())
                .user(user)
                .books(books)
                .histories(histories)
                .tokens(tokens)
                .build());

        tokenRepository.deleteAll(tokens);
        bookRepository.deleteAll(books);
        historyRepository.deleteAll(histories);
        repository.delete(user);
    }
}
