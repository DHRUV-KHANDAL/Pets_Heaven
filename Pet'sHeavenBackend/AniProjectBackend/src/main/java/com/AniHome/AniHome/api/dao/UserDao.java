package com.AniHome.AniHome.api.dao;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.AniHome.AniHome.api.entity.User;

@Repository
public interface UserDao extends MongoRepository<User, String> {

}
