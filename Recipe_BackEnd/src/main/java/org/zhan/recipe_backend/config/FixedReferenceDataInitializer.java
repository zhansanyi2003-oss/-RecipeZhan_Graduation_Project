package org.zhan.recipe_backend.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.zhan.recipe_backend.common.CourseEnum;
import org.zhan.recipe_backend.common.DietTypeEnum;
import org.zhan.recipe_backend.entity.Course;
import org.zhan.recipe_backend.entity.DietType;
import org.zhan.recipe_backend.repository.CourseRepository;
import org.zhan.recipe_backend.repository.DietTypeRepository;

@Component
public class FixedReferenceDataInitializer implements CommandLineRunner {

    private final CourseRepository courseRepository;
    private final DietTypeRepository dietTypeRepository;

    public FixedReferenceDataInitializer(CourseRepository courseRepository, DietTypeRepository dietTypeRepository) {
        this.courseRepository = courseRepository;
        this.dietTypeRepository = dietTypeRepository;
    }

    @Override
    @Transactional
    public void run(String... args) {
        for (CourseEnum course : CourseEnum.values()) {
            courseRepository.findByName(course.name())
                    .orElseGet(() -> courseRepository.save(Course.builder().name(course.name()).build()));
        }

        for (DietTypeEnum dietType : DietTypeEnum.values()) {
            dietTypeRepository.findByName(dietType.getLabel())
                    .orElseGet(() -> dietTypeRepository.save(DietType.builder().name(dietType.getLabel()).build()));
        }
    }
}
