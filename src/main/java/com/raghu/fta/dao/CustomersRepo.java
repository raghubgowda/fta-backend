package com.raghu.fta.dao;

import com.raghu.fta.model.Customer;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface CustomersRepo extends MongoRepository<Customer, String> {
}
