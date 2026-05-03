package org.zhan.recipe_backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.zhan.recipe_backend.entity.Course;
import org.zhan.recipe_backend.entity.Flavour;
import org.zhan.recipe_backend.entity.Recipe_Course;

import java.util.Optional;

@Repository
public interface CourseRepository extends JpaRepository<Course, Long>
{
    Optional<Course> findByName(String courseName);
}
