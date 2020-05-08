package com.raghu.fta.dao;

import com.raghu.fta.model.User;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;

public interface UsersRepo extends MongoRepository<User, String> {
    @Query("{ 'email' : ?0 }")
    List<User> findUsersByEmail(String email);
}
