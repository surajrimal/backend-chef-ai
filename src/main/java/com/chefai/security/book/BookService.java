package com.chefai.security.book;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import org.springframework.data.domain.AuditorAware;

@Service
@RequiredArgsConstructor
public class BookService {

    private final BookRepository repository;
    private final AuditorAware<String> auditorAware;

    public void save(BookRequest request) {
        var book = request.getId() == null
                ? Book.builder().build()
                : repository.findById(request.getId()).orElseGet(() -> Book.builder()
                        .id(request.getId())
                        .createdBy(auditorAware.getCurrentAuditor()
                                .orElseThrow(() -> new IllegalStateException(
                                        "Cannot save a book without an authenticated user")))
                        .build());

        book.setAuthor(request.getAuthor());
        book.setIsbn(request.getIsbn());
        book.setContent(request.getIsbn());
        repository.save(book);
    }

    public List<Book> findAll() {
        return repository.findAll();
    }
}
