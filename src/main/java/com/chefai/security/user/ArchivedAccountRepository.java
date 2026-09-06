package com.chefai.security.user;

import org.springframework.data.mongodb.repository.MongoRepository;

public interface ArchivedAccountRepository extends MongoRepository<ArchivedAccount, String> {
}
