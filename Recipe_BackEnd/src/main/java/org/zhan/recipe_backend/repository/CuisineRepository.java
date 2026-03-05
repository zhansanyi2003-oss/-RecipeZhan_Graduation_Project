package org.zhan.recipe_backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.zhan.recipe_backend.entity.Cuisine;
import org.zhan.recipe_backend.entity.Recipe_Cuisine;

import java.util.Optional;

@Repository
public interface CuisineRepository extends JpaRepository<Cuisine, Long>
{
    Optional<Cuisine> findByName(String cuisineName);
}
