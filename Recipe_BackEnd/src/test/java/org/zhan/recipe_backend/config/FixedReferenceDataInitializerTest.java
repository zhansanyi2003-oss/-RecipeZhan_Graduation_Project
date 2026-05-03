package org.zhan.recipe_backend.config;

import org.junit.jupiter.api.Test;
import org.zhan.recipe_backend.entity.Course;
import org.zhan.recipe_backend.entity.DietType;
import org.zhan.recipe_backend.repository.CourseRepository;
import org.zhan.recipe_backend.repository.DietTypeRepository;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class FixedReferenceDataInitializerTest {

    @Test
    void run_insertsMissingFixedCourseAndDietTypeValuesOnly() throws Exception {
        CourseRepository courseRepository = mock(CourseRepository.class);
        DietTypeRepository dietTypeRepository = mock(DietTypeRepository.class);
        FixedReferenceDataInitializer initializer = new FixedReferenceDataInitializer(courseRepository, dietTypeRepository);

        when(courseRepository.findByName("BREAKFAST")).thenReturn(Optional.empty());
        when(courseRepository.findByName("LUNCH")).thenReturn(Optional.of(Course.builder().name("LUNCH").build()));
        when(dietTypeRepository.findByName("Vegetarian")).thenReturn(Optional.empty());
        when(dietTypeRepository.findByName("Vegan")).thenReturn(Optional.of(DietType.builder().name("Vegan").build()));

        initializer.run();

        verify(courseRepository).save(argThat(course -> "BREAKFAST".equals(course.getName())));
        verify(courseRepository, never()).save(argThat(course -> "LUNCH".equals(course.getName())));
        verify(dietTypeRepository).save(argThat(dietType -> "Vegetarian".equals(dietType.getName())));
        verify(dietTypeRepository, never()).save(argThat(dietType -> "Vegan".equals(dietType.getName())));
    }
}
