package org.zhan.recipe_backend.repository;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.zhan.recipe_backend.entity.Recipe_Rating;

import java.util.Optional;

public interface RatingRepository extends JpaRepository<Recipe_Rating, Long> {



    Optional<Recipe_Rating> findByUserIdAndRecipeId(Long userId, Long recipeId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Integer countByRecipeId(Long recipeId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT COALESCE(AVG(r.score), 0.0) FROM Recipe_Rating r WHERE r.recipeId = :recipeId")
    Double getAverageByRecipeId(@Param("recipeId") Long recipeId);

    void deleteByRecipeId(Long recipeId);



}
