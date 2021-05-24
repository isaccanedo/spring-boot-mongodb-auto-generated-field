package com.isaccanedo.mongodb.file.daos;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.isaccanedo.mongodb.file.models.Photo;

public interface PhotoRepository extends MongoRepository<Photo, String> {

}
