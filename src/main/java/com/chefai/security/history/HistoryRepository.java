package com.chefai.security.history;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface HistoryRepository extends MongoRepository<History, String> {
    List<History> findAllByLastModifiedBy(String userId);

    long deleteByIdAndLastModifiedBy(String id, String userId);
}
