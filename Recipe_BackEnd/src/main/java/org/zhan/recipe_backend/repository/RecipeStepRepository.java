package org.zhan.recipe_backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.zhan.recipe_backend.entity.RecipeStep;

@Repository
public interface RecipeStepRepository  extends JpaRepository<RecipeStep, Long> {
}
