package org.zhan.recipe_backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.zhan.recipe_backend.entity.Recipe;
import org.zhan.recipe_backend.entity.RecipeRating;

import java.util.Optional;

public interface RatingRepository extends JpaRepository<RecipeRating, Long> {


    Optional<RecipeRating> findByUserIdAndRecipeId(Long userId, Long recipeId);
    Integer countByRecipeId(Long recipeId);
    @Query("SELECT COALESCE(AVG(r.score), 0.0) FROM RecipeRating r WHERE r.recipeId = :recipeId")
    Double calculateAverageByRecipeId(@Param("recipeId") Long recipeId);



}
