package org.zhan.recipe_backend.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.zhan.recipe_backend.entity.RecipeSearchSyncEvent;

import java.time.LocalDateTime;
import java.util.List;

public interface RecipeSearchSyncEventRepository extends JpaRepository<RecipeSearchSyncEvent, Long> {

    @Query("""
            select event
            from RecipeSearchSyncEvent event
            where event.status = org.zhan.recipe_backend.common.RecipeSearchSyncStatus.PENDING
              and event.nextRetryAt <= :now
            order by event.createdAt asc, event.id asc
            """)
    List<RecipeSearchSyncEvent> findDueEvents(@Param("now") LocalDateTime now, Pageable pageable);
}
