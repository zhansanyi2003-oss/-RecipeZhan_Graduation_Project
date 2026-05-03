package org.zhan.recipe_backend.repository;





import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.zhan.recipe_backend.entity.Recipe;

@Repository
public interface RecipeRepository extends JpaRepository<Recipe, Long> {

    // 🏆 最核心的方法：按创建时间倒序查询所有的食谱，并且返回轻量级的投影
    // Pageable 是用来做分页的参数

    Slice<Recipe> findByAuthorIdOrderByCreatedAtDesc(Long authorId, Pageable pageable);

    Slice<Recipe> findAllByOrderByCreatedAtDesc(Pageable pageable);

    Slice<Recipe> findByTitleContainingIgnoreCaseOrderByCreatedAtDesc(String title, Pageable pageable);

    int countByAuthorId(Long authorId);


}
