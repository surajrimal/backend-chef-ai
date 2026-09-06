package com.chefai.security.book;

import org.springframework.data.mongodb.repository.MongoRepository;

public interface BookRepository extends MongoRepository<Book, String> {

    long deleteByIdAndCreatedBy(String id, String userId);
}
