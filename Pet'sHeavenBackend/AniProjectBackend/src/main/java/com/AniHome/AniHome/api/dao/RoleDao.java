package com.AniHome.AniHome.api.dao;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.AniHome.AniHome.api.entity.Role;

@Repository
public interface RoleDao extends MongoRepository<Role, String> {

}
