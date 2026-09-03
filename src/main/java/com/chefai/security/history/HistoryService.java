package com.chefai.security.history;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class HistoryService {

    private final HistoryRepository repository;

    public void save(HistoryRequest request) {
        var history = History.builder()
                        .id(request.getId())
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
}
