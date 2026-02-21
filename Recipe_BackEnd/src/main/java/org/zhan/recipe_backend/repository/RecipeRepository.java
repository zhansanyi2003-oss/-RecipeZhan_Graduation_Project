package org.zhan.recipe_backend.repository;





import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.zhan.recipe_backend.dto.RecipeCardDto;
import org.zhan.recipe_backend.entity.Recipe;
import org.zhan.recipe_backend.projection.RecipeCardProjection;

import java.util.List;

@Repository
public interface RecipeRepository extends JpaRepository<Recipe, Long> {

    // 🏆 最核心的方法：按创建时间倒序查询所有的食谱，并且返回轻量级的投影
    // Pageable 是用来做分页的参数




    @Query(value="SELECT DISTINCT unnest(flavours) FROM recipes WHERE flavours IS NOT NULL", nativeQuery = true )
    List<String> findAllDistinctFlavours();

    @Query(value="SELECT DISTINCT unnest(cuisines) FROM recipes WHERE cuisines IS NOT NULL", nativeQuery = true )
    List<String> findAllDistinctCuisines();
}