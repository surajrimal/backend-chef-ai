package com.chefai.security.history;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
@RequiredArgsConstructor
public class HistoryService {

    private final HistoryRepository repository;

    public void save(HistoryRequest request) {
        var history = History.builder()
                        .id(request.getId())
                .ingredients(request.getIngredients())
                        .htmlPage(request.getHtml())

                        .build();
        repository.save(history);
    }

    public List<History> findAll() {
        return repository.findAll();
    }
    public List<History> findByUserId(String userId) {
        return repository.findAllByLastModifiedBy(userId);
    }

    public void delete(String id, String userId) {
        if (repository.deleteByIdAndLastModifiedBy(id, userId) == 0) {
            throw new ResponseStatusException(NOT_FOUND, "History not found");
        }
    }
}
