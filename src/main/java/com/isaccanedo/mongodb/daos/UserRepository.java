package com.isaccanedo.mongodb.daos;


import com.isaccanedo.mongodb.models.User;
import org.springframework.data.mongodb.repository.MongoRepository;


public interface UserRepository extends MongoRepository<User, Long> {

}
