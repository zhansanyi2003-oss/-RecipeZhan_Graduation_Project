package org.zhan.recipe_backend.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.zhan.recipe_backend.entity.Recipe;
import org.zhan.recipe_backend.entity.UserSavedRecipes;

public interface UserSavedRepository extends JpaRepository<UserSavedRecipes, Long> {

    boolean existsByUserIdAndRecipeId(Long userId, Long recipeId);


    void deleteByUserIdAndRecipeId(Long userId, Long recipeId);

    // 🌟 获取用户的收藏列表，支持按照收藏时间倒序，完美支持分页（Pageable）！
    @Query("SELECT s.recipe FROM UserSavedRecipes s WHERE s.user.id = :userId ORDER BY s.createdAt DESC")
    Slice<Recipe> findSavedRecipesByUserId(@Param("userId") Long userId, Pageable pageable);

    int countByUserId(Long currenUserId);
}
