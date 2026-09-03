package com.chefai.security.book;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.AuditorAware;

@ExtendWith(MockitoExtension.class)
class BookServiceTest {

    @Mock
    private BookRepository repository;

    @Mock
    private AuditorAware<String> auditorAware;

    @InjectMocks
    private BookService service;

    @Test
    void saveNewBookMapsRequestAndSavesBook() {
        var request = BookRequest.builder()
                .author("Author")
                .isbn("978-1")
                .content("Book content")
                .build();

        service.save(request);

        var captor = ArgumentCaptor.forClass(Book.class);
        verify(repository).save(captor.capture());
        var savedBook = captor.getValue();
        assertEquals("Author", savedBook.getAuthor());
        assertEquals("978-1", savedBook.getIsbn());
        assertEquals("Book content", savedBook.getContent());
    }

    @Test
    void saveExistingBookUpdatesBook() {
        var existingBook = Book.builder()
                .id("book-1")
                .createdBy("user-1")
                .build();
        when(repository.findById("book-1")).thenReturn(Optional.of(existingBook));
        var request = BookRequest.builder()
                .id("book-1")
                .author("Updated author")
                .isbn("978-2")
                .content("Updated content")
                .build();

        service.save(request);

        verify(repository).save(existingBook);
        assertEquals("user-1", existingBook.getCreatedBy());
        assertEquals("Updated author", existingBook.getAuthor());
        assertEquals("978-2", existingBook.getIsbn());
        assertEquals("Updated content", existingBook.getContent());
    }

    @Test
    void saveBookWithUnknownIdUsesCurrentAuditor() {
        when(repository.findById("book-1")).thenReturn(Optional.empty());
        when(auditorAware.getCurrentAuditor()).thenReturn(Optional.of("user-1"));
        var request = BookRequest.builder()
                .id("book-1")
                .author("Author")
                .isbn("978-1")
                .content("Content")
                .build();

        service.save(request);

        var captor = ArgumentCaptor.forClass(Book.class);
        verify(repository).save(captor.capture());
        assertEquals("book-1", captor.getValue().getId());
        assertEquals("user-1", captor.getValue().getCreatedBy());
    }

    @Test
    void saveBookWithUnknownIdFailsWithoutCurrentAuditor() {
        when(repository.findById("book-1")).thenReturn(Optional.empty());
        when(auditorAware.getCurrentAuditor()).thenReturn(Optional.empty());
        var request = BookRequest.builder().id("book-1").build();

        assertThrows(IllegalStateException.class, () -> service.save(request));
    }

    @Test
    void findAllReturnsBooksFromRepository() {
        var books = List.of(Book.builder().id("book-1").build());
        when(repository.findAll()).thenReturn(books);

        assertEquals(books, service.findAll());
        verify(repository).findAll();
    }
}
