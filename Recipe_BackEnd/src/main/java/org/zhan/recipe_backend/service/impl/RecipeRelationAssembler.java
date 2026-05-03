package org.zhan.recipe_backend.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.zhan.recipe_backend.common.CourseEnum;
import org.zhan.recipe_backend.common.DietTypeEnum;
import org.zhan.recipe_backend.dto.IngredientDto;
import org.zhan.recipe_backend.dto.RecipeRequestDto;
import org.zhan.recipe_backend.dto.RecipeStepDto;
import org.zhan.recipe_backend.entity.Course;
import org.zhan.recipe_backend.entity.DietType;
import org.zhan.recipe_backend.entity.Ingredient;
import org.zhan.recipe_backend.entity.Recipe;
import org.zhan.recipe_backend.entity.RecipeStep;
import org.zhan.recipe_backend.entity.Recipe_Course;
import org.zhan.recipe_backend.entity.Recipe_Cuisine;
import org.zhan.recipe_backend.entity.Recipe_DietType;
import org.zhan.recipe_backend.entity.Recipe_Flavour;
import org.zhan.recipe_backend.entity.Recipe_Ingredient;
import org.zhan.recipe_backend.repository.CourseRepository;
import org.zhan.recipe_backend.repository.DietTypeRepository;
import org.zhan.recipe_backend.repository.IngredientRepository;
import org.zhan.recipe_backend.service.RecipeTaxonomyService;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.stream.Collectors;

@Service
public class RecipeRelationAssembler {

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private IngredientRepository ingredientRepository;

    @Autowired
    private DietTypeRepository dietTypeRepository;

    @Autowired
    private RecipeTaxonomyService recipeTaxonomyService;

    public void applyRelations(Recipe recipe, RecipeRequestDto dto) {
        syncCollection(recipe.getRecipeFlavours(), buildFlavours(dto.getFlavours(), recipe), recipe::setRecipeFlavours);
        syncCollection(recipe.getRecipeCourses(), buildCourses(dto.getCourses(), recipe), recipe::setRecipeCourses);
        syncCollection(recipe.getRecipeCuisines(), buildCuisines(dto.getCuisines(), recipe), recipe::setRecipeCuisines);
        syncCollection(recipe.getRecipeDietTypes(), buildDietTypes(dto.getDietTypes(), recipe), recipe::setRecipeDietTypes);
        syncCollection(recipe.getRecipeIngredients(), buildIngredients(dto.getIngredients(), recipe), recipe::setRecipeIngredients);
        syncCollection(recipe.getSteps(), buildSteps(dto.getSteps(), recipe), recipe::setSteps);
    }

    private List<Recipe_Flavour> buildFlavours(List<String> names, Recipe recipe) {
        if (names == null) return new ArrayList<>();
        return names.stream()
                .map(recipeTaxonomyService::getOrCreateFlavour)
                .filter(Objects::nonNull)
                .distinct()
                .map(flavour -> {
                    Recipe_Flavour relation = new Recipe_Flavour();
                    relation.setRecipe(recipe);
                    relation.setFlavour(flavour);
                    return relation;
                }).collect(Collectors.toList());
    }

    private List<Recipe_Course> buildCourses(List<CourseEnum> courses, Recipe recipe) {
        if (courses == null) return new ArrayList<>();
        return courses.stream().map(courseEnum -> {
            Course course = courseRepository.findByName(courseEnum.name())
                    .orElseThrow(() -> new IllegalStateException("Course seed data missing: " + courseEnum.name()));
            Recipe_Course relation = new Recipe_Course();
            relation.setRecipe(recipe);
            relation.setCourse(course);
            return relation;
        }).collect(Collectors.toList());
    }

    private List<Recipe_Cuisine> buildCuisines(List<String> names, Recipe recipe) {
        if (names == null) return new ArrayList<>();
        return names.stream()
                .map(recipeTaxonomyService::getOrCreateCuisine)
                .filter(Objects::nonNull)
                .distinct()
                .map(cuisine -> {
                    Recipe_Cuisine relation = new Recipe_Cuisine();
                    relation.setRecipe(recipe);
                    relation.setCuisine(cuisine);
                    return relation;
                }).collect(Collectors.toList());
    }

    private List<Recipe_DietType> buildDietTypes(List<DietTypeEnum> dietTypes, Recipe recipe) {
        if (dietTypes == null) return new ArrayList<>();
        return dietTypes.stream().map(dietTypeEnum -> {
            DietType dietType = dietTypeRepository.findByName(dietTypeEnum.getLabel())
                    .orElseThrow(() -> new IllegalStateException("Diet type seed data missing: " + dietTypeEnum.getLabel()));
            Recipe_DietType relation = new Recipe_DietType();
            relation.setRecipe(recipe);
            relation.setDietType(dietType);
            return relation;
        }).collect(Collectors.toList());
    }

    private List<Recipe_Ingredient> buildIngredients(List<IngredientDto> dtos, Recipe recipe) {
        if (dtos == null) return new ArrayList<>();
        return dtos.stream().map(ingDto -> {
            Ingredient ingredient = ingredientRepository.findByName(ingDto.getName())
                    .orElseGet(() -> ingredientRepository.save(Ingredient.builder().name(ingDto.getName()).build()));
            Recipe_Ingredient relation = new Recipe_Ingredient();
            relation.setRecipe(recipe);
            relation.setIngredient(ingredient);
            relation.setAmount(ingDto.getAmount());
            relation.setNote(ingDto.getNote());
            return relation;
        }).collect(Collectors.toList());
    }

    private List<RecipeStep> buildSteps(List<RecipeStepDto> dtos, Recipe recipe) {
        if (dtos == null) return new ArrayList<>();
        List<RecipeStep> steps = new ArrayList<>();
        for (int i = 0; i < dtos.size(); i++) {
            RecipeStepDto stepDto = dtos.get(i);
            RecipeStep step = new RecipeStep();
            step.setRecipe(recipe);
            step.setStepNumber(i + 1);
            step.setContent(stepDto.getContent());
            step.setImageUrls(stepDto.getImageUrls());
            steps.add(step);
        }
        return steps;
    }

    private <T> void syncCollection(List<T> current, List<T> replacement, Consumer<List<T>> setter) {
        if (current == null) {
            setter.accept(replacement);
            return;
        }
        current.clear();
        current.addAll(replacement);
    }
}
