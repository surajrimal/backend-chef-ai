package com.chefai.security.history;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class HistoryServiceTest {

    @Mock
    private HistoryRepository repository;

    @InjectMocks
    private HistoryService service;

    @Test
    void saveMapsRequestAndSavesHistory() {
        var request = HistoryRequest.builder()
                .id("history-1")
                .html("<h1>Recipe</h1>")
                .build();

        service.save(request);

        var captor = ArgumentCaptor.forClass(History.class);
        verify(repository).save(captor.capture());
        assertEquals("history-1", captor.getValue().getId());
        assertEquals("<h1>Recipe</h1>", captor.getValue().getHtmlPage());
    }

    @Test
    void findAllReturnsHistoryFromRepository() {
        var histories = List.of(History.builder().id("history-1").build());
        when(repository.findAll()).thenReturn(histories);

        assertEquals(histories, service.findAll());
        verify(repository).findAll();
    }

    @Test
    void findByUserIdReturnsHistoryFromRepository() {
        var histories = List.of(History.builder().id("history-1").lastModifiedBy("user-1").build());
        when(repository.findAllByLastModifiedBy("user-1")).thenReturn(histories);

        assertEquals(histories, service.findByUserId("user-1"));
        verify(repository).findAllByLastModifiedBy("user-1");
    }
}
