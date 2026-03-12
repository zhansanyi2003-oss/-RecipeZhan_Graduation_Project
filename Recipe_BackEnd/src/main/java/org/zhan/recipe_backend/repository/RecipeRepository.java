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

    List<Recipe> findByAuthorIdOrderByCreatedAtDesc(Long authorId);

    int countByAuthorId(Long authorId);


}