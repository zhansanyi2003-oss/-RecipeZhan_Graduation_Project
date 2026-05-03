package org.zhan.recipe_backend.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;
import org.zhan.recipe_backend.entity.Recipe;
import org.zhan.recipe_backend.entity.UserSavedRecipes;

import java.util.List;
import java.util.Set;

public interface UserSavedRepository extends JpaRepository<UserSavedRecipes, Long> {

    boolean existsByUserIdAndRecipeId(Long userId, Long recipeId);


    void deleteByUserIdAndRecipeId(Long userId, Long recipeId);

    void deleteByRecipeId(Long recipeId);

    // 🌟 获取用户的收藏列表，支持按照收藏时间倒序，完美支持分页（Pageable）！
    @Query("SELECT s.recipe FROM UserSavedRecipes s WHERE s.user.id = :userId ORDER BY s.createdAt DESC")
    Slice<Recipe> findSavedRecipesByUserId(@Param("userId") Long userId, Pageable pageable);

    int countByUserId(Long currenUserId);

    @Query("select s.recipe.id from UserSavedRecipes  s where s.user.id=:userId ")
    Set<Long> findRecipeIdsByUserId(@Param("userId")Long currenUserId);

    @Modifying
    @Query(value = """
        insert into user_saved_recipes (user_id, recipe_id)
        values (:userId, :recipeId)
        on conflict (user_id, recipe_id) do nothing
        """,
            nativeQuery = true
    )
    void saveRecipe(@Param("userId") Long userId, @Param("recipeId") Long recipeId);



}
