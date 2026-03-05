package org.zhan.recipe_backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.zhan.recipe_backend.entity.Ingredient;
import org.zhan.recipe_backend.entity.Recipe_Ingredient;

import java.util.Optional;

@Repository
public interface IngredientRepository extends JpaRepository<Ingredient, Long>
{
    Optional<Ingredient> findByName(String name);
}
