package org.zhan.recipe_backend.repository;





import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.zhan.recipe_backend.dto.RecipeCardDto;
import org.zhan.recipe_backend.entity.Recipe;

import java.util.List;
import java.util.Optional;

@Repository
public interface RecipeRepository extends JpaRepository<Recipe, Long> {

    // 🏆 最核心的方法：按创建时间倒序查询所有的食谱，并且返回轻量级的投影
    // Pageable 是用来做分页的参数
    @Query(value = """
        SELECT * FROM recipes r WHERE 
        
        (CAST(:#{#cardParam.title} AS text) IS NULL OR\s
                     -- 1. 传统模糊匹配 (查局部的词)
                     r.title ILIKE CONCAT('%', CAST(:#{#cardParam.title} AS text), '%') OR\s
                     -- 2. FTS 全文检索 (查语法变形、语序颠倒、单复数，比如 fried chicken)
                     to_tsvector('english', r.title) @@ websearch_to_tsquery('english', CAST(:#{#cardParam.title} AS text)) OR
                     -- 3. Trigram 错别字容错 (查拼写错误，比如 chiken)
                     word_similarity(CAST(:#{#cardParam.title} AS text), r.title) > 0.3) AND

        
        (CAST(:maxTime AS int) IS NULL OR r.cooking_time_min <= CAST(:maxTime AS int)) AND 
        (CAST(:minTime AS int) IS NULL OR r.cooking_time_min > CAST(:minTime AS int)) AND 
        
        (CAST(:#{#cardParam.flavoursStr} AS text) IS NULL OR r.flavours && CAST(string_to_array(CAST(:#{#cardParam.flavoursStr} AS text), ',') AS varchar[])) AND 
        (CAST(:#{#cardParam.cuisinesStr} AS text) IS NULL OR r.cuisines && CAST(string_to_array(CAST(:#{#cardParam.cuisinesStr} AS text), ',') AS varchar[])) AND 
        (CAST(:#{#cardParam.coursesStr} AS text) IS NULL OR r.courses && CAST(string_to_array(CAST(:#{#cardParam.coursesStr} AS text), ',') AS varchar[])) AND 
        
        (CAST(:#{#cardParam.ingredientsStr} AS text) IS NULL OR r.ingredient_tags @> CAST(string_to_array(CAST(:#{#cardParam.ingredientsStr} AS text), ',') AS varchar[]))
        """,
            nativeQuery = true)
    Slice<Recipe> searchRecipesByParams(
            @Param("cardParam") RecipeCardDto cardParam,
            @Param("minTime") Integer minTime,
            @Param("maxTime") Integer maxTime,
            Pageable pageable
    );
    @Query(value="SELECT DISTINCT unnest(flavours) FROM recipes WHERE flavours IS NOT NULL", nativeQuery = true )
    List<String> findAllDistinctFlavours();

    @Query(value="SELECT DISTINCT unnest(cuisines) FROM recipes WHERE cuisines IS NOT NULL", nativeQuery = true )
    List<String> findAllDistinctCuisines();



}