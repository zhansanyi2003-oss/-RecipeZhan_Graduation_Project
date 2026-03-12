package org.zhan.recipe_backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.zhan.recipe_backend.entity.Cuisine;
import org.zhan.recipe_backend.entity.DietType;

import java.util.Optional;

@Repository
public interface DietTypeRepository extends JpaRepository<DietType, Long> {
    Optional<DietType> findByName(String name);
}
