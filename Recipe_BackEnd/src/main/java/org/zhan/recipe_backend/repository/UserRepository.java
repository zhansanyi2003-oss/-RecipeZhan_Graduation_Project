package org.zhan.recipe_backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.zhan.recipe_backend.entity.User;

// UserRepository.java
@Repository
public interface UserRepository extends JpaRepository<User, Long> {


    User findByUsername(String username);
}