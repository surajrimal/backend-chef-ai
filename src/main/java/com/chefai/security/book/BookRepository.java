package com.chefai.security.book;

import java.util.List;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface BookRepository extends MongoRepository<Book, String> {

    List<Book> findAllByCreatedBy(String userId);

    long deleteByIdAndCreatedBy(String id, String userId);
}
