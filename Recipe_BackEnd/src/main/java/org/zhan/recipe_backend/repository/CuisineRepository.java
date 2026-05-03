package org.zhan.recipe_backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.zhan.recipe_backend.entity.Cuisine;

import java.util.Optional;

@Repository
public interface CuisineRepository extends JpaRepository<Cuisine, Long>
{
    Optional<Cuisine> findByName(String cuisineName);

    @Modifying
    @Query(value = """
            insert into cuisines (name)
            values (:name)
            on conflict (name) do nothing
            """,
            nativeQuery = true
    )
    int insertIgnoreByName(@Param("name") String name);
}
