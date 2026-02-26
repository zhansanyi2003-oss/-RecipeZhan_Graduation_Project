package org.zhan.recipe_backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.zhan.recipe_backend.entity.User;

// UserRepository.java
public interface UserRepository extends JpaRepository<User, Long> {
}